package com.ebiggz.postalservice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.mailbox.Mailbox;

public class PlayerOpenMailMenuEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private User accountOwner;
	private Mailbox mailbox;

	public PlayerOpenMailMenuEvent(Player player, User accountOwner, Mailbox mailbox) {
		this.player = player;
		this.accountOwner = accountOwner;
		this.mailbox = mailbox;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public User getAccountOwner() {
		return accountOwner;
	}

	public void setAccountOwner(User accountOwner) {
		this.accountOwner = accountOwner;
	}

	public boolean usingMailbox() {
		return mailbox != null;
	}

	public Mailbox getMailbox() {
		return mailbox;
	}

}
