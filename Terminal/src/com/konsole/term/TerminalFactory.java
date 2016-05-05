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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class TerminalFactory {

    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N
    public static final Map<String, TerminalTopComponent> openedTerminals = new HashMap<>();

    public static void closeOthers(List<String> hosts) {
        Map<String, TerminalTopComponent> toBeClosed = new HashMap<>(openedTerminals);
        toBeClosed.keySet().removeAll(hosts);
        for (TerminalTopComponent openedTerminal : toBeClosed.values()) {
            openedTerminal.close();
        }
        openedTerminals.keySet().retainAll(hosts);
    }

    public static void closeAll() {
        for (TerminalTopComponent openedTerminal : new ArrayList<>(openedTerminals.values())) {
            openedTerminal.close();
        }
    }

    public static TerminalTopComponent newTerminalTopComponent(final String title, CountDownLatch latch) {
        if (openedTerminals.containsKey(title)) {
            latch.countDown();
            return openedTerminals.get(title);
        }
        TerminalTopComponent emulator = new TerminalTopComponent() {
            @Override
            protected void componentClosed() {
                super.componentClosed();
                openedTerminals.remove(title);
            }
        };
        openedTerminals.put(title, emulator);
        WindowManager.getDefault().findMode("editor").dockInto(emulator);

        emulator.open();

        IOContainer ioContainer = emulator.getIOContainer();
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        if (env != null) {
            String homeDir = System.getProperty("user.home");
            openTerminalImpl(ioContainer, title, env, homeDir, true, latch);
        }
        return emulator;
    }

    private static void openTerminalImpl(final IOContainer ioContainer, final String tabTitle, final ExecutionEnvironment env, final String dir, final boolean silentMode, final CountDownLatch latch) {

        final IOProvider term = IOProvider.get("Terminal"); // NOI18N

        if (term != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        ioContainer.requestActive();
                        latch.countDown();
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
                        ioRef.set(term.getIO(tabTitle, null, ioContainer));
                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                        String shell = hostInfo.getLoginShell();
                        if (dir != null) {
                            npb.setWorkingDirectory(dir);
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
            RP.post(runnable);
        }
    }
}
