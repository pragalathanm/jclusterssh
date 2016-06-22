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

    class TextCommand implements Command {

        public final String text;

        public TextCommand(String text) {
            assert text != null;
            this.text = text;
        }

        @Override
        public boolean isHistoryCommand() {
            return !text.equals("clear") && !text.trim().isEmpty();
        }

        @Override
        public void execute(TerminalExecution terminal) throws IOException {
            Writer out = terminal.getOutput();
            out.write(text);
            out.write(KeyEvent.VK_ENTER);
            out.flush();
        }
    }

    abstract class SpecialCommand implements Command {

        private final int keyCode;
        private final char c;
        private final int keyChar;

        public SpecialCommand(int keyCode, char c, int keyChar) {
            this.keyCode = keyCode;
            this.c = c;
            this.keyChar = keyChar;
        }

        @Override
        public void execute(TerminalExecution terminal) {
            JComponent comp = terminal.getComponent();
            Runnable r = () -> {
                comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, keyCode, c));
                comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_TYPED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_UNDEFINED, (char) keyChar));
                comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, keyCode, c));
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }
    }

    class CtrlCCommand extends SpecialCommand {

        public CtrlCCommand() {
            super(KeyEvent.VK_C, 'c', 3);
        }
    }

    class ClearCommand extends SpecialCommand {

        public ClearCommand() {
            super(KeyEvent.VK_L, 'l', 0x000c);
        }
    }

    interface TerminalExecution {

        Writer getOutput();

        JComponent getComponent();
    }
}
