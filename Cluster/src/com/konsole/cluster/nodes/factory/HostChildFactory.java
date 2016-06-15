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
package com.konsole.cluster.nodes.factory;

import com.konsole.term.Host;
import com.konsole.cluster.nodes.HostNode;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Node;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HostChildFactory extends AbstractChildFactory<Host> {

    public HostChildFactory() {
        super(new ArrayList<Host>());
    }

    public HostChildFactory(List<Host> items) {
        super(items);
    }

    @Override
    protected Node createNodeForKey(Host key) {
        return new HostNode(key);
    }
}
