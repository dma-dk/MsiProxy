/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.provider.dkmsinm.service;

import dk.dma.msiproxy.model.JsonSerializable;
import dk.dma.msiproxy.model.msi.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a search result as returned from the MSI-NM server
 */
public class MessageSearchResult implements JsonSerializable {

    List<Message> messages = new ArrayList<>();
    int startIndex;
    int total;
    boolean overflowed;


    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isOverflowed() {
        return overflowed;
    }

    public void setOverflowed(boolean overflowed) {
        this.overflowed = overflowed;
    }
}
