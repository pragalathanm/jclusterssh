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
package com.konsole.cluster.lookup;

import com.konsole.cluster.Cluster;
import com.konsole.cluster.ClusterPanel;
import com.konsole.cluster.CommandPanel;
import com.konsole.cluster.cookie.ClusterCookie;
import com.konsole.term.Command;
import org.netbeans.modules.openide.windows.GlobalActionContextImpl;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@ServiceProvider(
        service = ContextGlobalProvider.class,
        supersedes = "org.netbeans.modules.openide.windows.GlobalActionContextImpl")
public class GlobalActionContextProxy implements ContextGlobalProvider {

    /**
     * The proxy lookup returned by this class:
     */
    private Lookup proxyLookup;
    /**
     * The native NetBeans global context lookup provider and the official
     * global lookup managed by the NetBeans Platform:
     */
    private final GlobalActionContextImpl globalContextProvider;
    private final Lookup globalContextLookup;
    /**
     * Additional customer content for our custom global lookup:
     */
    private Lookup lookupContainer;
    private final InstanceContent ic;
    private final Result<Command> commandResult;
    private final Result<Cluster> clusterResult;
    private final Result<ClusterCookie> clusterCookieResult;

    public GlobalActionContextProxy() {
        this.ic = new InstanceContent();
        // The default GlobalContextProvider:
        this.globalContextProvider = new GlobalActionContextImpl();
        this.globalContextLookup = this.globalContextProvider.createGlobalContext();
        // Monitor the existance of a Cluster in the official ClusterPanel lookup:
        this.clusterResult = ClusterPanel.getInstance().getLookup().lookupResult(Cluster.class);
        this.clusterResult.addLookupListener(new LookupAdapter<>(clusterResult, Cluster.class));
        this.clusterCookieResult = ClusterPanel.getInstance().getLookup().lookupResult(ClusterCookie.class);
        this.clusterCookieResult.addLookupListener(new LookupAdapter<>(clusterCookieResult, ClusterCookie.class));
        this.commandResult = CommandPanel.getInstance().getLookup().lookupResult(Command.class);
        this.commandResult.addLookupListener(new LookupAdapter<>(commandResult, Command.class));
    }

    private class LookupAdapter<T> implements LookupListener {

        private final Object lock = new Object();
        private final Result<T> result;
        private final Class<T> type;

        @SuppressWarnings("rawtypes")
        LookupAdapter(Result<T> result, Class<T> type) {
            this.result = result;
            this.type = type;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void resultChanged(LookupEvent ev) {
            synchronized (lock) {
                removeFromLookup(type);
                if (result.allInstances().size() > 0) {
                    T item = result.allInstances().iterator().next();
                    addToLookup(item);
                }
            }
        }
    }

    private <T> void removeFromLookup(Class<T> clazz) {
        // clear the existing content in lookup
        lookupContainer.lookupAll(clazz).stream().forEach((c) -> {
            ic.remove(c);
        });
    }

    private <T> void addToLookup(T content) {
        // add the selection to lookup
        ic.add(content);
    }

    /**
     * Returns a ProxyLookup that adds the current Cluster instance to the
     * global selection returned by Utilities.actionsGlobalContext().
     *
     * @return a ProxyLookup that includes the original global context lookup.
     */
    @Override
    public Lookup createGlobalContext() {
        if (proxyLookup == null) {
            // Create the two lookups that will make up the proxy:
            lookupContainer = new AbstractLookup(ic);
            proxyLookup = new ProxyLookup(globalContextLookup, lookupContainer);
        }
        return proxyLookup;
    }
}
