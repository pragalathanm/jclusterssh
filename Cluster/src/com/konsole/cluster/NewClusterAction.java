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

import com.konsole.term.TerminalTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@ActionID(
        category = "File",
        id = "com.konsole.cluster.NewClusterAction"
)
@ActionRegistration(
        displayName = "#CTL_NewClusterAction"
)
@ActionReference(path = "Menu/File", position = 0, separatorBefore = -50, separatorAfter = 50)
@Messages("CTL_NewClusterAction=New Cluster")
public final class NewClusterAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        TerminalTopComponent tc = (TerminalTopComponent) WindowManager.getDefault().findMode("editor").getSelectedTopComponent();
        tc.execute("ls");

    }
}
