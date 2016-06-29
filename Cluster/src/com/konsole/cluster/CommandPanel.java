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
package com.konsole.cluster;

import com.konsole.term.Command;
import com.konsole.term.TerminalCookie;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.JTextField;
import org.openide.util.AsyncGUIJob;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.KeyEvent.KEY_RELEASED;
import static java.awt.event.KeyEvent.KEY_TYPED;
import static java.awt.event.KeyEvent.VK_V;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class CommandPanel extends TopComponent {

    private Lookup.Result<TerminalCookie> terminalCookieResult;
    private static final CommandPanel INSTANCE = new CommandPanel();
    private final InstanceContent ic = new InstanceContent();
    private static final int CONTROL = Utilities.isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
    private static final int CONTROL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static CommandPanel getInstance() {
        return INSTANCE;
    }

    /**
     * Creates new form CommandPanel
     */
    public CommandPanel() {
        initComponents();
        associateLookup(new AbstractLookup(ic));
        commandTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                clearLookup();
            }
        });
        Utilities.attachInitJob(this, new AsyncGUIJob() {

            @Override
            public void construct() {
                // calling this method later to avoid cicular dependency on GlobalActionContextProxy class
                addListeners();
            }

            @Override
            public void finished() {
            }
        });
        commandTextField.addKeyListener(
                new KeyListener() {

                    @Override
                    public void keyTyped(java.awt.event.KeyEvent evt) {
                        if (isInteractiveMode()) {
                            updateLookup(Command.newInteractiveCommand(evt));
                        }
                    }

                    @Override
                    public void keyPressed(java.awt.event.KeyEvent evt) {
                        if (isInteractiveMode()) {
                            updateLookup(Command.newInteractiveCommand(evt));
                        }
                    }

                    @Override
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        if (isInteractiveMode()) {
                            updateLookup(Command.newInteractiveCommand(evt));
                            commandTextField.setText("");
                        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            executeCommand(commandTextField.getText());
                        } else if (evt.isControlDown()) {
                            if (evt.getKeyCode() == KeyEvent.VK_L) {
                                updateLookup(Command.newClearCommand());
                            } else if (evt.getKeyCode() == KeyEvent.VK_C) {
                                updateLookup(Command.newCtrlCCommand());
                            }
                        } else if (commandTextField.getText().trim().isEmpty()) {
                            switch (evt.getKeyCode()) {
                                case KeyEvent.VK_LEFT:
                                case KeyEvent.VK_RIGHT:
                                case KeyEvent.VK_UP:
                                case KeyEvent.VK_DOWN:
                                case KeyEvent.VK_END:
                                case KeyEvent.VK_HOME:
                                case KeyEvent.VK_PAGE_UP:
                                case KeyEvent.VK_PAGE_DOWN:
                                    updateLookup(Command.newSpecialCommand(evt.getKeyCode()));
                                    break;
                                case KeyEvent.VK_ESCAPE:
                                    updateLookup(Command.newEscapeCommand());
                                    break;
                            }
                        }
                    }
                }
        );
    }

    public boolean isInteractiveMode() {
        return interactiveModeCheckBox.isSelected();
    }

    public void addListeners() {
        this.terminalCookieResult = Utilities.actionsGlobalContext().lookupResult(TerminalCookie.class);
        this.terminalCookieResult.addLookupListener((LookupEvent ev) -> {
            commandTextField.setEnabled(!terminalCookieResult.allInstances().isEmpty());
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commandTextField = new HintTextField();
        interactiveModeCheckBox = new javax.swing.JCheckBox();

        commandTextField.setText(org.openide.util.NbBundle.getMessage(CommandPanel.class, "CommandPanel.commandTextField.text")); // NOI18N
        commandTextField.setToolTipText(org.openide.util.NbBundle.getMessage(CommandPanel.class, "CommandPanel.commandTextField.toolTipText")); // NOI18N
        commandTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(interactiveModeCheckBox, org.openide.util.NbBundle.getMessage(CommandPanel.class, "CommandPanel.interactiveModeCheckBox.text")); // NOI18N
        interactiveModeCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(CommandPanel.class, "CommandPanel.interactiveModeCheckBox.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(interactiveModeCheckBox))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(interactiveModeCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void executeCommand(String command) {
        executeCommand(command, true);
    }

    private void executeCommand(String command, boolean clearTextField) {
        if (clearTextField) {
            commandTextField.setText("");
        }
        updateLookup(Command.newCommand(command));
    }

    private void updateLookup(Command command) {
        clearLookup();
        ic.add(command);
    }

    private void clearLookup() {
        Command cmd = getLookup().lookup(Command.class);
        if (cmd != null) {
            ic.remove(cmd);
        }
    }

    class HintTextField extends JTextField {

        private final String hint = org.openide.util.NbBundle.getMessage(CommandPanel.class, "CommandPanel.commandTextField.toolTipText");
        private final PasteQueue pasteQueue = new PasteQueue();

        @Override
        protected void processKeyEvent(KeyEvent evt) {
            if (!isInteractiveMode()) {
                super.processKeyEvent(evt);
                return;
            }
            if (pasteQueue.isEmpty()) {
                if (evt.getID() == KEY_PRESSED && evt.getKeyCode() == CONTROL) {
                    pasteQueue.offer(evt);
                    return;
                }
                super.processKeyEvent(evt);
            } else {
                if (!pasteQueue.isExpected(evt)) { // this event is not part of paste-command sequence
                    while (!pasteQueue.isEmpty()) {
                        super.processKeyEvent(pasteQueue.poll());
                    }
                    super.processKeyEvent(evt);
                } else {
                    pasteQueue.offer(evt);
                    if (pasteQueue.size() == 5) {
                        // we have received all the key events. clear it now.
                        pasteQueue.clear();
                        try {
                            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                                updateLookup(Command.newPasteCommand(text));
                            }
                        } catch (UnsupportedFlavorException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().length() == 0 && !hasFocus()) {
                g.setColor(Color.LIGHT_GRAY);
                int padding = (getHeight() - getFont().getSize()) / 2;
                int inset = 3;
                g.drawString(hint, inset + 5, getHeight() - padding - inset);
            }
        }

        class PasteQueue extends LinkedList<KeyEvent> {

            boolean isExpected(KeyEvent e2) {
                KeyEvent e1 = peekLast();
                if (e1.getID() == KEY_PRESSED && e1.getKeyCode() == CONTROL) {
                    return (e2.getID() == KEY_PRESSED && e2.getKeyCode() == VK_V && e2.getModifiers() == CONTROL_MASK);
                } else if (e1.getID() == KEY_PRESSED && e1.getKeyCode() == VK_V && e1.getModifiers() == CONTROL_MASK) {
                    return (e2.getID() == KEY_TYPED && e2.getKeyCode() == 0 && e2.getModifiers() == CONTROL_MASK);
                } else if (e1.getID() == KEY_TYPED && e1.getKeyCode() == 0 && e1.getModifiers() == CONTROL_MASK) {
                    return (e2.getID() == KEY_RELEASED && (e2.getKeyCode() == VK_V && e2.getModifiers() == CONTROL_MASK) || e2.getKeyCode() == CONTROL);
                } else if (e1.getID() == KEY_RELEASED && e1.getKeyCode() == VK_V && e1.getModifiers() == CONTROL_MASK) {
                    return (e2.getID() == KEY_RELEASED && e2.getKeyCode() == CONTROL);
                } else if (e1.getKeyCode() == CONTROL) {
                    return (e2.getID() == KEY_RELEASED && e2.getKeyCode() == VK_V);
                }
                return false;
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField commandTextField;
    private javax.swing.JCheckBox interactiveModeCheckBox;
    // End of variables declaration//GEN-END:variables
}
