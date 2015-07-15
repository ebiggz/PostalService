package com.gmail.erikbigler.postalservice.backend;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mail.MailType;

public interface User {

	public UUID getUUID();

	public String getPlayerName();

	public void setPlayerName(String playerName);

	public Mail[] getInbox();

	public Mail[] getSentbox();

	public ItemStack[] getDropbox();

	public void saveDropbox(List<ItemStack> items);

	public Mail[] getBoxFromType(BoxType type);

	public int getUnreadMailCount();

	public boolean sendMail(String recipient, String message,
			String attachmentData, MailType mailType);

	public boolean receieveMail(long mailID, Player sender, MailType mailType);

	public boolean markAllMailAsRead();

	public boolean markMailAsClaimed(Mail mail);

	public boolean markMailAsDeleted(Mail mail);

	public int getMaxMailboxCount();

	public int getMaxInboxSize();

}
