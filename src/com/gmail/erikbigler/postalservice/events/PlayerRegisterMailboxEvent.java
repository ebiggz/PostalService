package com.gmail.erikbigler.postalservice.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRegisterMailboxEvent extends Event {

	private Player player;
	private Location location;
	private boolean canceled;

	private static final HandlerList handlers = new HandlerList();

	public PlayerRegisterMailboxEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLocation() {
		return location;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
