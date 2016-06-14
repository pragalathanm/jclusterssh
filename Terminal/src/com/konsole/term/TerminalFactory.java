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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class TerminalFactory {

    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N
    private static final Map<String, TerminalTopComponent> openedTerminals = new HashMap<>();

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

    public static TerminalTopComponent newTerminalTopComponent(final String title) {
        if (openedTerminals.containsKey(title)) {
            return openedTerminals.get(title);
        }
        TerminalTopComponent emulator = new TerminalTopComponent(title) {
            @Override
            protected void componentClosed() {
                super.componentClosed();
                openedTerminals.remove(title);
            }

            @Override
            protected void componentOpened() {
                super.componentOpened();
                openedTerminals.put(title, this);
            }
        };

        WindowManager.getDefault().findMode("editor").dockInto(emulator);

        emulator.open();
        RP.post(emulator.connect());
        return emulator;
    }
}
