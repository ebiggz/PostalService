package com.gmail.erikbigler.postalservice.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.config.WorldGroup;
import com.gmail.erikbigler.postalservice.mail.MailType;

public class PlayerSendMailEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private User sender;
	private User recipient;
	private String message;
	private String attachmentData;
	private MailType mailType;
	private WorldGroup worldGroup;
	private boolean cancelled;


	public PlayerSendMailEvent(User sender, User recipient, String message, String attachmentData, MailType mailType, WorldGroup worldGroup) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.attachmentData = attachmentData;
		this.mailType = mailType;
		this.worldGroup = worldGroup;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public User getSender() {
		return sender;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAttachmentData() {
		return attachmentData;
	}

	public void setAttachmentData(String attachmentData) {
		this.attachmentData = attachmentData;
	}

	public MailType getMailType() {
		return mailType;
	}

	public void setMailType(MailType mailType) {
		this.mailType = mailType;
	}

	public WorldGroup getWorldGroup() {
		return worldGroup;
	}

	public void setWorldGroup(WorldGroup worldGroup) {
		this.worldGroup = worldGroup;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
