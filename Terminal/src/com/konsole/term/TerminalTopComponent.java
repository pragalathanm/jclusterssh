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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
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
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor2;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService2;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
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
    private Lookup.Result<Command> lookupResult;
    private final ExecutorService executor = Executors.newSingleThreadExecutor((Runnable r) -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });
    private Future<Integer> ptyProcess;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public TerminalTopComponent(String title) {
        setLayout(new BorderLayout());
        tc = TerminalContainer.create(TerminalTopComponent.this, "Local");
        add(tc, BorderLayout.CENTER);
        this.title = title;
        setName(title);
        associateLookup(Lookups.fixed(new TerminalCookie() {
        }));

        makeBusy(true);

        lookupResult = Utilities.actionsGlobalContext().lookupResult(Command.class);
        lookupResult.addLookupListener((LookupEvent ev) -> {
            Collection<? extends Command> result = lookupResult.allInstances();
            if (!result.isEmpty()) {
                execute(result.iterator().next().text);
            }
        });
        executor.submit(connect());
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

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    private Runnable connect() {
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
        Runnable runnable = () -> {
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
                NativeExecutionDescriptor2 descr = new NativeExecutionDescriptor2().controllable(true).frontWindow(true).inputVisible(true).inputOutput(ioRef.get());
                descr.postExecution(() -> {
                    ioRef.get().closeInputOutput();
                });
                NativeExecutionService2 es = NativeExecutionService2.newService(npb, descr, "Terminal Emulator"); // NOI18N
                ptyProcess = es.run(() -> {
                    executor.submit(new CommandExecutorTask());
                });
                SwingUtilities.invokeAndWait(() -> ioContainer.requestActive());
            } catch (CancellationException | InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        return runnable;
    }

    class CommandExecutorTask implements Runnable {

        @Override
        public void run() {
            Callable<Optional<OutputStreamWriter>> runnable = () -> {
                Terminal terminal = (Terminal) getIOContainer().getSelected();
                ActiveTerm at = terminal.term();
                try {
                    return Optional.of(at.getOutputStreamWriter());
                } catch (IllegalStateException ex) {
                    // Error: getOutputStreamWriter() can only be used after connect()
                    at.getOut().write("\nConnecting...");
                    return Optional.empty();
                }
            };

            try {
                RunnableFuture<Optional<OutputStreamWriter>> connectionChecker = new FutureTask<>(runnable);
                SwingUtilities.invokeLater(connectionChecker);
                Optional<OutputStreamWriter> writerWrapper = connectionChecker.get();

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
        }
    }
}
