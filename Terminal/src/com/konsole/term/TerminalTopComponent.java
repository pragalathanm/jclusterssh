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
package com.konsole.term;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class TerminalTopComponent extends TopComponent {

    private final TerminalContainer tc;
    private final String title;

    public TerminalTopComponent(String title) {
        setLayout(new BorderLayout());
        tc = TerminalContainer.create(TerminalTopComponent.this, "Local");
        add(tc, BorderLayout.CENTER);
        this.title = title;
        setName(title);
    }

    @Override
    public void setDisplayName(String name) {
        super.setName(title);
    }

    @Override
    public void setHtmlDisplayName(String htmlDisplayName) {
        super.setName(title);
    }

    public IOContainer getIOContainer() {
        return tc.ioContainer();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        tc.componentActivated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        tc.componentDeactivated();
    }

    public void execute(String command) {
        Terminal terminal = (Terminal) getIOContainer().getSelected();
        ActiveTerm at = terminal.term();
        try {
            OutputStreamWriter outputStreamWriter = at.getOutputStreamWriter();
            outputStreamWriter.write(command);
            outputStreamWriter.write(KeyEvent.VK_ENTER);
            outputStreamWriter.flush();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
//    public void clear() {
//        Terminal terminal = (Terminal) getIOContainer().getSelected();
//        ActiveTerm at = terminal.term();
//        try {
//            OutputStreamWriter outputStreamWriter = at.getOutputStreamWriter();
//            outputStreamWriter.write('clear');
//            outputStreamWriter.write(KeyEvent.VK_ENTER);
//            outputStreamWriter.flush();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
}
