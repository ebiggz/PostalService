package com.ebiggz.postalservice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.ebiggz.postalservice.backend.User;

public class PlayerUnregisterAllMailboxesEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private User mailboxOwner;
	private boolean canceled;

	public PlayerUnregisterAllMailboxesEvent(Player player, User mailboxOwner) {
		this.player = player;
		this.mailboxOwner = mailboxOwner;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public User getMailboxesOwner() {
		return mailboxOwner;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
