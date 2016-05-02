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

import com.konsole.cluster.nodes.DefaultNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Pragalathan M
 */
public abstract class AbstractChildFactory<T> extends ChildFactory<T> {

    private List<T> entries;

    public AbstractChildFactory(List<T> items) {
        this.entries = items;
    }

    @Override
    protected boolean createKeys(List<T> toPopulate) {
        if (entries != null) {
            toPopulate.addAll(entries);
        } else {
            entries = new ArrayList<>();
        }
        return true;
    }

    @Override
    protected abstract Node createNodeForKey(T key);

    public void addEntry(T name) {
        entries.add(name);
        refresh(true);
    }

    public void addEntry(List<T> names) {
        entries.addAll(names);
        refresh(true);
    }

    public void removeAll() {
        entries = Collections.emptyList();
        refresh(true);
    }

    public void removeEntry(T name) {
        entries.remove(name);
        refresh(true);
    }

    public void removeEntry(Node... nodes) {
        for (Node node : nodes) {
            entries.remove((T) ((DefaultNode) node).getUserObject());
        }
        refresh(true);
    }

    public void removeEntry(List<T> names) {
        entries.removeAll(names);
        refresh(true);
    }

    public void setEntries(List<T> list) {
        this.entries = list;
        this.refresh(true);
    }

    public List<T> getEntries() {
        return entries;
    }

    public void reload() {
        this.refresh(true);
    }
}
