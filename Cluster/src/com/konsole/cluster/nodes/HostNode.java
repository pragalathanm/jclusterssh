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
package com.konsole.cluster.nodes;

import com.konsole.cluster.host.Host;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HostNode extends DefaultNode<Host> {

    private final Host host;

    public HostNode(Host userObject) {
        super(userObject);
        host = userObject;
        setName(host.getName());
        setIconBaseWithExtension("com/konsole/cluster/images/host.png");
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        try {
            ss.put(new PropertySupport.Reflection<>(host, String.class, "name"));
            ss.put(new PropertySupport.Reflection<>(host, String.class, "ipAddress"));
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return s;
    }
}
