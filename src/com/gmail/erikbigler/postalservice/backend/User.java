package com.gmail.erikbigler.postalservice.backend;

import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.InboxType;


public interface User {

	public UUID getUUID();

	public String getPlayerName();

	public void setPlayerName(String playerName);

	public Mail[] getInbox();

	public Mail[] getSentbox();

	public ItemStack[] getDropbox();

	public void saveDropbox(List<ItemStack> items);

	public Mail[] getBoxFromType(InboxType type);

	public int getUnreadMailCount();

	public boolean sendMail(String recipient, String message, String attachmentData, String mailType);

	public boolean receieveMail(String from, String message, String attachmentData, String mailType);

	public boolean markMailAsRead();

	public boolean markMailAsClaimed(Mail mail);

	public boolean markMailAsDeleted(Mail mail);

	public int getMaxMailboxCount();

	public int getMaxInboxSize();

}
