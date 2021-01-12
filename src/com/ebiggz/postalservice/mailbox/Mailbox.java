package com.ebiggz.postalservice.mailbox;

import org.bukkit.Location;
import org.bukkit.block.Chest;

import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;

public class Mailbox {

	private Location location;
	private String playerID;

	public Mailbox(Location location, String playerID) {
		this.location = location;
		this.playerID = playerID;
	}

	/** @return the location of this mailbox */
	public Location getLocation() {
		return location;
	}

	/** @return the mailbox's owner */
	public User getOwner() {
		return UserFactory.getUserFromIdentifier(playerID);
	}
	
	public Chest getChest() {
		return ((Chest) this.location.getBlock().getState());
	}
}
