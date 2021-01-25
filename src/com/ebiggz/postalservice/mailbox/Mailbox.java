package com.ebiggz.postalservice.mailbox;

import org.bukkit.Location;
import org.bukkit.block.Chest;

import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;

public class Mailbox {

	private Location location;
	private String playerID;
	private boolean isPostOffice;

	public Mailbox(Location location, String playerID, boolean isPostOffice) {
		this.location = location;
		this.playerID = playerID;
		this.isPostOffice = isPostOffice;
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

	public boolean isPostOffice() {
		return isPostOffice;
	}

	public void setIsPostOffice(boolean isPostOffice) {
		this.isPostOffice = isPostOffice;
	}
}
