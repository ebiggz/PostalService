package com.gmail.erikbigler.postalservice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.gmail.erikbigler.postalservice.mailbox.Mailbox;

public class PlayerUnregisterMailboxEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Mailbox mailbox;
	private boolean canceled;

	public PlayerUnregisterMailboxEvent(Player player, Mailbox mailbox) {
		this.player = player;
		this.mailbox = mailbox;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public Mailbox getMailbox() {
		return mailbox;
	}

	public void setMailbox(Mailbox mailbox) {
		this.mailbox = mailbox;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
