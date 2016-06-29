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

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Writer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public interface Command {

    public default boolean isHistoryCommand() {
        return false;
    }

    void execute(TerminalExecution terminal) throws IOException;

    public static TextCommand newCommand(String text) {
        return new TextCommand(text);
    }

    public static TextCommand newPasteCommand(String text) {
        return new TextCommand(text, false);
    }

    public static TextCommand newSshCommand(Host host) {
        StringBuilder test = new StringBuilder("ssh ");
        if (host.getUser() != null && !host.getUser().isEmpty()) {
            test.append(host.getUser()).append("@");
        }
        test.append(host.getIpAddress());
        return new TextCommand(test.toString());
    }

    public static Command newCtrlCCommand() {
        return new CtrlCCommand();
    }

    public static Command newClearCommand() {
        return new ClearCommand();
    }

    public static Command newSpecialCommand(int keyCode) {
        return new SpecialKeyCommand(keyCode);
    }

    public static Command newInteractiveCommand(KeyEvent evt) {
        return new InteractiveCommand(evt);
    }

    public static Command newEscapeCommand() {
        return new EscapeKeyCommand();
    }

    class TextCommand implements Command {

        public final String text;
        private boolean includeNewLine;

        public TextCommand(String text) {
            this(text, true);
        }

        public TextCommand(String text, boolean includeNewLine) {
            assert text != null;
            this.text = text;
            this.includeNewLine = includeNewLine;
        }

        @Override
        public boolean isHistoryCommand() {
            return !text.equals("clear") && !text.trim().isEmpty();
        }

        @Override
        public void execute(TerminalExecution terminal) throws IOException {
            Writer out = terminal.getOutput();
            out.write(text);
            if (includeNewLine) {
                out.write(KeyEvent.VK_ENTER);
            }
            out.flush();
        }
    }

    abstract class KeyCommand implements Command {

        @Override
        public void execute(TerminalExecution terminal) {
            Runnable r = () -> {
                execute(terminal.getComponent());
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }

        protected abstract void execute(JComponent comp);
    }

    class SpecialKeyCommand extends KeyCommand {

        private final int keyCode;

        private SpecialKeyCommand(int keyCode) {
            this.keyCode = keyCode;
        }

        @Override
        public void execute(JComponent comp) {
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED));
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED));
        }
    }

    public class InteractiveCommand extends KeyCommand {

        public KeyEvent evt;

        public InteractiveCommand(KeyEvent evt) {
            this.evt = evt;
        }

        @Override
        protected void execute(JComponent comp) {
            comp.dispatchEvent(new KeyEvent(comp, evt.getID(), System.currentTimeMillis(), evt.getModifiers(), evt.getKeyCode(), evt.getKeyChar()));
        }

        @Override
        public String toString() {
            return "InteractiveCommand{" + "evt=" + evt + '}';
        }
    }

    class EscapeKeyCommand extends KeyCommand {

        @Override
        public void execute(JComponent comp) {
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, 0, (char) 0x001b));
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
        }
    }

    abstract class ControlKeyCommand extends KeyCommand {

        private final int keyCode;
        private final char c;
        private final int keyChar;

        public ControlKeyCommand(int keyCode, char c, int keyChar) {
            this.keyCode = keyCode;
            this.c = c;
            this.keyChar = keyChar;
        }

        @Override
        public void execute(JComponent comp) {
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, keyCode, c));
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_TYPED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_UNDEFINED, (char) keyChar));
            comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, keyCode, c));
        }
    }

    class CtrlCCommand extends ControlKeyCommand {

        public CtrlCCommand() {
            super(KeyEvent.VK_C, 'c', 3);
        }
    }

    class ClearCommand extends ControlKeyCommand {

        public ClearCommand() {
            super(KeyEvent.VK_L, 'l', 0x000c);
        }
    }

    interface TerminalExecution {

        Writer getOutput();

        JComponent getComponent();
    }
}
