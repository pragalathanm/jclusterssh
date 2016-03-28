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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbPreferences;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class StoreManager {

    public static List<Cluster> clusters;

    public static List<Cluster> getClusters() throws IOException, ClassNotFoundException {
        byte[] buffer = NbPreferences.forModule(StoreManager.class).getByteArray("clusters", new byte[0]);
        if (buffer.length == 0) {
            System.out.println("no cluster saved");
            return new ArrayList<>();
        }
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
            return (List<Cluster>) stream.readObject();
        }
    }

    public static void setClusters(List<Cluster> clusters) throws IOException {
        System.err.println("saving clusters = " + clusters);
        if (clusters == null) {
            clusters = new ArrayList<>();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(out)) {
            stream.writeObject(clusters);
        }
        NbPreferences.forModule(StoreManager.class).putByteArray("clusters", out.toByteArray());
    }
}
