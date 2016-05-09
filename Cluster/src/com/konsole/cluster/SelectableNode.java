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

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import javax.swing.SwingUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class SelectableNode extends AbstractNode {

    private ExplorerManager em;
    private AbstractNode dummy;

    public SelectableNode(Children children) {
        super(children);
    }

    public SelectableNode(Children children, ExplorerManager explorerManager) {
        super(children);
        try {
            dummy = new BeanNode<>("");
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }

        this.em = explorerManager;
    }

    public void selectNode(final int index) {
        try {
            if (getChildren().getNodesCount() > index && index >= 0) {
                if (em.getRootContext() == dummy) {
                    em.setRootContext(SelectableNode.this);
                    SwingUtilities.invokeLater(() -> {
                        selectNode(index);
                    });
                } else {
                    em.setSelectedNodes(new Node[]{getChildren().getNodeAt(index)});
                }
            } else {
                em.setRootContext(dummy);
            }
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void selectFirstNode() {
        SwingUtilities.invokeLater(() -> {
            selectNode(0);
        });
    }

    public void selectLastNode() {
        SwingUtilities.invokeLater(() -> {
            selectNode(getChildren().getNodesCount() - 1);
        });
    }
}
