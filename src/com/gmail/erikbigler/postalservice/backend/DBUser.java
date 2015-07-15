package com.gmail.erikbigler.postalservice.backend;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mail.MailType;

public class DBUser implements User {

	private UUID uuid;
	private String playerName;

	public DBUser(String playerName) {
		this.playerName = playerName;
		// accuire uuid
	}

	public DBUser(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public String getPlayerName() {
		return playerName;
	}

	@Override
	public void setPlayerName(String playerName) {

	}

	@Override
	public Mail[] getInbox() {
		return null;
	}

	@Override
	public Mail[] getSentbox() {
		return null;
	}

	@Override
	public ItemStack[] getDropbox() {
		return null;
	}

	@Override
	public void saveDropbox(List<ItemStack> items) {

	}

	@Override
	public Mail[] getBoxFromType(BoxType type) {
		if (type == BoxType.INBOX) {
			return getInbox();
		} else {
			return getSentbox();
		}
	}

	@Override
	public int getUnreadMailCount() {
		return 0;
	}

	@Override
	public boolean sendMail(String recipient, String message,
			String attachmentData, MailType mailType) {
		// save mail to ps_mail
		// get auto incremented mail id
		UserFactory.getUser(recipient).receieveMail(0/* mailID */, Bukkit.getPlayer(getUUID()), mailType);
		return false;
	}

	@Override
	public boolean receieveMail(long mailID, Player sender, MailType mailType) {
		// save to ps_received. 0 for status, 0 for deleted
		// send message to user saying they have received mail
		return false;
	}

	@Override
	public boolean markAllMailAsRead() {
		return false;
	}

	@Override
	public boolean markMailAsClaimed(Mail mail) {
		return false;
	}

	@Override
	public boolean markMailAsDeleted(Mail mail) {
		return false;
	}

	@Override
	public int getMaxMailboxCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxInboxSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
