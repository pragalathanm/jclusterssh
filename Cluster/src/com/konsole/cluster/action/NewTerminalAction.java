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

import com.konsole.term.TerminalTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "com.konsole.cluster.action.NewTerminalAction"
)
@ActionRegistration(
        iconBase = "com/konsole/cluster/images/new_terminal.png",
        displayName = "#CTL_NewTerminalAction"
)
@ActionReference(path = "Menu/File", position = -90)
@Messages("CTL_NewTerminalAction=New Terminal")
public final class NewTerminalAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new TerminalTopComponent().open();
    }
}
