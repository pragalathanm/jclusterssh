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

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class Command {

    public final String text;

    public Command(String text) {
        assert text != null;
        this.text = text;
    }

    public boolean isHistoryCommand() {
        return !text.equals("clear") && !text.trim().isEmpty();
    }

    public static Command newSshCommand(Host host) {
        StringBuilder test = new StringBuilder("ssh ");
        if (host.getUser() != null && !host.getUser().isEmpty()) {
            test.append(host.getUser()).append("@");
        }
        test.append(host.getIpAddress());
        return new Command(test.toString());
    }
}
