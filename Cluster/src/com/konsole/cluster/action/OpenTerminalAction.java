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
package com.konsole.cluster.action;

import com.konsole.term.Command;
import com.konsole.term.Host;
import com.konsole.term.TerminalFactory;
import com.konsole.term.TerminalTopComponent;
import java.awt.event.ActionEvent;
import java.util.Optional;
import javax.swing.AbstractAction;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class OpenTerminalAction extends AbstractAction {

    private final Host host;

    public OpenTerminalAction(Host host) {
        super("Open Terminal");
        this.host = host;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<TerminalTopComponent> terminal = TerminalFactory.getTerminalTopComponent(host);
        if (!terminal.isPresent()) {
            TerminalTopComponent tc = TerminalFactory.newTerminalTopComponent(host);
            tc.execute(Command.newSshCommand(host));
        } else {
            terminal.get().requestActive();
        }
    }
}
