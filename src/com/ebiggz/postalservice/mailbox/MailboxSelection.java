package com.ebiggz.postalservice.mailbox;

public class MailboxSelection {

    private String ownerName;
    private MailboxSelectionType selectionType;

    public MailboxSelection(String mailboxOwnerName, MailboxSelectionType type) {
        this.ownerName = mailboxOwnerName;
        this.selectionType = type;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public MailboxSelectionType getSelectionType() {
        return selectionType;
    }
}
