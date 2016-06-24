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

import com.konsole.cluster.cookie.ClusterCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "com.konsole.cluster.action.CloseClusterAction"
)
@ActionRegistration(
        iconBase = "com/konsole/cluster/images/close.png",
        displayName = "#CTL_CloseClusterAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 22),
    @ActionReference(path = "Toolbars/XAction", position = 250)
})
@Messages("CTL_CloseClusterAction=Close Cluster")
public final class CloseClusterAction implements ActionListener {

    private final ClusterCookie context;

    public CloseClusterAction(ClusterCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.close();
    }
}
