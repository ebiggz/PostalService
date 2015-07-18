package com.gmail.erikbigler.postalservice.backend;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.worldgroups.WorldGroup;

public interface User {

	public boolean isReal();

	public void createUser();

	public String getIdentifier();

	public UUID getUUID();

	public String getPlayerName();

	public void setPlayerName(String playerName);

	public Mail[] getInbox(WorldGroup worldGroup);

	public Mail[] getSentbox(WorldGroup worldGroup);

	public ItemStack[] getDropbox(WorldGroup worldGroup);

	public void saveDropbox(List<ItemStack> items, WorldGroup worldGroup);

	public Mail[] getBoxFromType(BoxType type, WorldGroup worldGroup);

	public int getUnreadMailCount(WorldGroup worldGroup);

	public boolean inboxIsFull(WorldGroup worldGroup);

	public boolean sendMail(String recipient, String message, String attachmentData, MailType mailType, WorldGroup worldGroup);

	public boolean receieveMail(long mailID, Player sender, MailType mailType, WorldGroup worldGroup);

	public boolean markAllMailAsRead(WorldGroup worldGroup);

	public boolean markMailAsClaimed(Mail mail);

	public boolean markMailAsDeleted(Mail mail);

}
