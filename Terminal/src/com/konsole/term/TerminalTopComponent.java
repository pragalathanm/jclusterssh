/*
 * Copyright (C) 2016 Pragalathan M <pragalathanm@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.konsole.term;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class TerminalTopComponent extends TopComponent {

    private final TerminalContainer tc;
    private final String title;
    private final ExecutorService executor = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private final TerminalCookie cookie = new TerminalCookie() {
    };

    public TerminalTopComponent(String title) {
        setLayout(new BorderLayout());
        tc = TerminalContainer.create(TerminalTopComponent.this, "Local");
        add(tc, BorderLayout.CENTER);
        this.title = title;
        setName(title);
        associateLookup(Lookups.fixed(cookie));

        executor.submit(() -> {
            RunnableFuture<Optional<OutputStreamWriter>> connectionChecker = new FutureTask<>(() -> {
                makeBusy(true);
                Terminal terminal = (Terminal) getIOContainer().getSelected();
                ActiveTerm at = terminal.term();
                at.getOut().write("Connecting...\n");
                try {
                    return Optional.of(at.getOutputStreamWriter());
                } catch (IllegalStateException ex) {
                    // Error: getOutputStreamWriter() can only be used after connect()
                    return Optional.empty();
                }
            });

            try {
                Optional<OutputStreamWriter> writerWrapper = null;
                do {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    SwingUtilities.invokeLater(connectionChecker);
                    writerWrapper = connectionChecker.get();
                } while (!writerWrapper.isPresent());

                makeBusy(false);

                OutputStreamWriter outputStreamWriter = writerWrapper.get();
                while (true && !Thread.interrupted()) {
                    try {
                        String command = queue.take();
                        outputStreamWriter.write(command);
                        outputStreamWriter.write(KeyEvent.VK_ENTER);
                        outputStreamWriter.flush();
                    } catch (IOException | InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public void setDisplayName(String name) {
        super.setName(title);
    }

    @Override
    public void setHtmlDisplayName(String htmlDisplayName) {
        super.setName(title);
    }

    public IOContainer getIOContainer() {
        return tc.ioContainer();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        tc.componentActivated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        tc.componentDeactivated();
    }

    public void execute(String command) {
        queue.offer(command);
    }
//    public void clear() {
//        Terminal terminal = (Terminal) getIOContainer().getSelected();
//        ActiveTerm at = terminal.term();
//        try {
//            OutputStreamWriter outputStreamWriter = at.getOutputStreamWriter();
//            outputStreamWriter.write('clear');
//            outputStreamWriter.write(KeyEvent.VK_ENTER);
//            outputStreamWriter.flush();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public Runnable connect() {
        final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        if (env == null) {
            throw new IllegalStateException("env is null");
        }

        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        if (term == null) {
            throw new IllegalStateException("IOProvider is null");
        }

        final String homeDir = System.getProperty("user.home");
        final boolean silentMode = true;
        final IOContainer ioContainer = getIOContainer();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    ioContainer.requestActive();
                } else {
                    doWork();
                }
            }

            private void doWork() {
                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                    try {
                        ConnectionManager.getInstance().connectTo(env);
                    } catch (IOException | ConnectionManager.CancellationException ex) {
                        return;
                    }
                }

                final HostInfo hostInfo;
                try {
                    hostInfo = HostInfoUtils.getHostInfo(env);
                    boolean isSupported = PtySupport.isSupportedFor(env);
                    if (!isSupported) {
                        if (!silentMode) {
                            String message;
                            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                                message = NbBundle.getMessage(TerminalFactory.class, "LocalTerminalNotSupported.error.nocygwin"); // NOI18N
                            } else {
                                message = NbBundle.getMessage(TerminalFactory.class, "LocalTerminalNotSupported.error"); // NOI18N
                            }
                            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                        }
                        return;
                    }
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }

                final AtomicReference<InputOutput> ioRef = new AtomicReference<>();
                try {
                    ioRef.set(term.getIO(title, null, ioContainer));
                    NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                    String shell = hostInfo.getLoginShell();
                    if (homeDir != null) {
                        npb.setWorkingDirectory(homeDir);
                    }
                    npb.setExecutable(shell);
                    NativeExecutionDescriptor descr;
                    descr = new NativeExecutionDescriptor().controllable(true).frontWindow(true).inputVisible(true).inputOutput(ioRef.get());
                    descr.postExecution(new Runnable() {
                        @Override
                        public void run() {
                            ioRef.get().closeInputOutput();
                        }
                    });
                    NativeExecutionService es = NativeExecutionService.newService(npb, descr, "Terminal Emulator"); // NOI18N
                    Future<Integer> result = es.run();
                    SwingUtilities.invokeLater(this);
                } catch (java.util.concurrent.CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        return runnable;
    }
}
