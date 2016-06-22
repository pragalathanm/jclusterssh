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

import com.konsole.term.Command.TerminalExecution;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
    private Future<?> commandLoop;
    private BlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    private boolean snoozed;

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
            if (!snoozed && !result.isEmpty()) {
                execute(result.iterator().next());
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

    public boolean isSnoozed() {
        return snoozed;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
        if (snoozed) {
            super.setHtmlDisplayName("<html><b><i>" + title + "</i></b></html>");
        } else {
            super.setHtmlDisplayName(title);
        }
    }

    public IOContainer getIOContainer() {
        return tc.ioContainer();
    }

    public void dispose() {
        commandLoop.cancel(true);
        executor.submit(() -> ptyProcess.cancel(true));
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

    public void execute(Command command) {
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
                    commandLoop = executor.submit(new CommandExecutorTask());
                });
                SwingUtilities.invokeAndWait(() -> ioContainer.requestActive());
            } catch (CancellationException | InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        return runnable;
    }

    class CommandExecutorTask implements Runnable {

        private void addMouseListener(Terminal terminal, JComponent comp) {
            // remove the old listener first
            for (MouseListener ml : comp.getMouseListeners()) {
                if (ml.getClass().getEnclosingClass() == org.netbeans.modules.terminal.ioprovider.Terminal.class) {
                    comp.removeMouseListener(ml);
                    break;
                }
            }
            comp.addMouseListener(new PopupListener(terminal, comp));
        }

        @Override
        public void run() {
            Callable<Optional<TerminalExecution>> runnable = () -> {
                Terminal terminal = (Terminal) getIOContainer().getSelected();
                try {
                    return Optional.of(new TerminalExecutionImpl(terminal));
                } catch (IllegalStateException ex) {
                    // Error: getOutputStreamWriter() can only be used after connect()
                    terminal.term().getOut().write("\nConnecting...");
                    return Optional.empty();
                }
            };

            try {
                RunnableFuture<Optional<TerminalExecution>> connectionChecker = new FutureTask<>(runnable);
                SwingUtilities.invokeLater(connectionChecker);
                Optional<TerminalExecution> wrapper = connectionChecker.get();

                makeBusy(false);

                TerminalExecutionImpl execution = (TerminalExecutionImpl) wrapper.get();
                execution.getTerminal().setClosable(false);
                addMouseListener(execution.getTerminal(), execution.getComponent());
                while (true && !Thread.interrupted()) {
                    try {
                        queue.take().execute(execution);
                    } catch (InterruptedException ex) {
                        break;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (ExecutionException | InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    class PopupListener extends MouseAdapter {

        private final Terminal terminal;
        private final JComponent comp;

        private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";	// NOI18N
        private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";	// NOI18N

        public PopupListener(Terminal terminal, JComponent comp) {
            this.terminal = terminal;
            this.comp = comp;
            createActions(terminal);
        }

        private boolean isBooleanStateAction(Action a) {
            Boolean isBooleanStateAction = (Boolean) a.getValue(BOOLEAN_STATE_ACTION_KEY);	//
            return isBooleanStateAction != null && isBooleanStateAction;
        }

        // On UNIX popup on press
        // On Windows popup on release.
        // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4119064
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), comp);
                postPopupMenu(p, terminal, comp);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), comp);
                postPopupMenu(p, terminal, comp);
            }
        }

        private void addMenuItem(JPopupMenu menu, Object o) {
            if (o instanceof JSeparator) {
                menu.add((JSeparator) o);
            } else if (o instanceof Action) {
                Action a = (Action) o;
                if (isBooleanStateAction(a)) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
                    item.setSelected((Boolean) a.getValue(BOOLEAN_STATE_ENABLED_KEY));
                    menu.add(item);
                } else {
                    menu.add((Action) o);
                }
            }
        }

        private void postPopupMenu(Point p, Terminal terminal, JComponent comp) {
            JPopupMenu menu = new JPopupMenu();
            menu.putClientProperty("container", getIOContainer()); // NOI18N
            menu.putClientProperty("component", terminal);             // NOI18N

            addMenuItem(menu, copyAction);
            addMenuItem(menu, pasteAction);
            addMenuItem(menu, new JSeparator());
            addMenuItem(menu, wrapAction);
            addMenuItem(menu, largerFontAction);
            addMenuItem(menu, smallerFontAction);
            addMenuItem(menu, new JSeparator());
            addMenuItem(menu, clearAction);
            addMenuItem(menu, new JSeparator());
            addMenuItem(menu, snoozeAction);

            menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
            menu.show(comp, p.x, p.y);
        }

        private void createActions(Terminal terminal) {
            Class<?>[] actions = Terminal.class.getDeclaredClasses();
            try {
                for (Class<?> a : actions) {
                    if (!AbstractAction.class.isAssignableFrom(a)) {
                        continue;
                    }
                    Constructor<?> constructor = a.getConstructor(Terminal.class);
                    constructor.setAccessible(true);
                    Action action = (Action) constructor.newInstance(terminal);
                    switch (a.getSimpleName()) {
                        case "CopyAction":
                            copyAction = action;
                            break;
                        case "PasteAction":
                            pasteAction = action;
                            break;
                        case "ClearAction":
                            clearAction = action;
                            break;
                        case "WrapAction":
                            wrapAction = action;
                            break;
                        case "LargerFontAction":
                            largerFontAction = action;
                            break;
                        case "SmallerFontAction":
                            smallerFontAction = action;
                            break;
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            snoozeAction = new AbstractAction("Snooze") {
                {
                    putValue(BOOLEAN_STATE_ACTION_KEY, true);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    setSnoozed(!isSnoozed());
                }

                @Override
                public Object getValue(String key) {
                    if (key.equals(BOOLEAN_STATE_ENABLED_KEY)) {
                        return isSnoozed();
                    } else {
                        return super.getValue(key);
                    }
                }
            };
        }
        private Action copyAction;
        private Action pasteAction;
        private Action snoozeAction;
        private Action wrapAction;
        private Action clearAction;
        private Action largerFontAction;
        private Action smallerFontAction;
    }

    class TerminalExecutionImpl implements Command.TerminalExecution {

        private Writer output;
        private JComponent component;
        private Terminal terminal;

        public TerminalExecutionImpl(Terminal terminal) {
            ActiveTerm term = terminal.term();
            this.output = term.getOutputStreamWriter();
            this.component = term.getScreen();
            this.terminal = terminal;
        }

        @Override
        public Writer getOutput() {
            return output;
        }

        @Override
        public JComponent getComponent() {
            return component;
        }

        public Terminal getTerminal() {
            return terminal;
        }
    }
}
