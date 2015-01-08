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
