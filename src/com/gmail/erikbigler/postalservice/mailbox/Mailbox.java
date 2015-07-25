package com.gmail.erikbigler.postalservice.mailbox;

import org.bukkit.Location;

import com.gmail.erikbigler.postalservice.backend.User;

public class Mailbox {

	private Location location;
	private User owner;

	public Mailbox(Location location, User owner) {
		this.location = location;
		this.owner = owner;
	}

	/** @return the location of this mailbox */
	public Location getLocation() {
		return location;
	}

	/** @return the mailbox's owner */
	public User getOwner() {
		return owner;
	}
}
