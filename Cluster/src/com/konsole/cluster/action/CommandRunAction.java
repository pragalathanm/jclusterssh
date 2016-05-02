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

import com.konsole.cluster.CommandPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@ActionID(
        category = "Command",
        id = "com.konsole.cluster.action.CommandRunAction"
)
@ActionRegistration(
        lazy = false,
        displayName = "#CTL_CommandRunAction"
)
@ActionReference(path = "Toolbars/ZAction", position = 500)
@Messages("CTL_CommandRunAction=Command Run")
public final class CommandRunAction extends AbstractAction implements Presenter.Toolbar {

    @Override
    public Component getToolbarPresenter() {
        return CommandPanel.getInstance();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
