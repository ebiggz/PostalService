package com.gmail.erikbigler.postalservice.mailbox;

import org.bukkit.Location;

import com.gmail.erikbigler.postalservice.backend.User;

public class Mailbox {

	private User owner;
	private Location location;
	private int mailboxID;

	public Mailbox(User owner, Location location, int mailboxID) {
		this.owner = owner;
		this.location = location;
		this.mailboxID = mailboxID;
	}

	/**
	 * @return the mailbox's owner
	 */
	public User getOwner() {
		return owner;
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
