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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());

    @Override
    public void restored() {
        try {
            LookAndFeel selectedLaf = UIManager.getLookAndFeel();
            if (selectedLaf.getName().equals("Nimbus")) {
                return;
            }
            if (selectedLaf.getName().equals("Metal")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOG.log(Level.INFO, "Error setting native LAF: {0}", e);
        }
//        System.getProperties().put("Term.debug", "keys");
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            new Thread() {
                @Override
                public void run() {
                    ClusterPanel.getInstance().loadClusters();
                }
            }.start();
        });
    }

    @Override
    public boolean closing() {
        ClusterPanel.getInstance().storeClusters();
        ((CommandTopComponent) WindowManager.getDefault().findTopComponent(CommandTopComponent.ID)).storeHistory();
        return true;
    }
}
