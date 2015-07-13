package com.gmail.erikbigler.postalservice.mailbox;

import org.bukkit.Location;


public class Mailbox {

	private String ownerName;
	private Location location;
	private int mailboxID;

	public Mailbox(String ownerName, Location location, int mailboxID) {
		this.ownerName = ownerName;
		this.location = location;
		this.mailboxID = mailboxID;
	}

	/**
	 * @return the mailbox's owner
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * @return the location of this mailbox
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return the id used in the database
	 */
	public int getMailboxID() {
		return mailboxID;
	}
}
