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

import com.konsole.term.Host;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class Cluster implements Serializable {

    private String name;
    private List<Host> hosts = new ArrayList<>();

    public Cluster(String name) {
        this.name = name;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
