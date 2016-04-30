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

import com.konsole.cluster.cookie.HostCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@ActionID(
        category = "File",
        id = "com.konsole.cluster.action.RemoveHostAction"
)
@ActionRegistration(
        iconBase = "com/konsole/cluster/images/remove_host.png",
        displayName = "#CTL_RemoveHostAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Host", position = 75),
    @ActionReference(path = "Toolbars/Host", position = 75)
})
@Messages("CTL_RemoveHostAction=Remove host")
public final class RemoveHostAction implements ActionListener {

    private final List<HostCookie> context;

    public RemoveHostAction(List<HostCookie> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (HostCookie hostCookie : context) {
            // TODO use hostCookie
        }
    }
}
