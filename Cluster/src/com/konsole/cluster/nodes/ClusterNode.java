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

import com.konsole.cluster.Cluster;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class ClusterNode extends DefaultNode<Cluster> {

    private final Cluster cluster;

    public ClusterNode(Cluster userObject) {
        super(userObject);
        cluster = userObject;
        setName(cluster.getName());
    }
}
