package com.gmail.erikbigler.postalservice.backend;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.configs.UUIDUtils;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.worldgroups.WorldGroup;

public class DBUser implements User {

	private UUID uuid;
	private String playerName;
	private boolean isReal = true;

	public DBUser(String playerName) {
		this.playerName = playerName;
		if (Config.USE_UUIDS) {
			UUID id = UUIDUtils.findUUID(playerName);
			if (id != null) {
				this.uuid = id;
				UUIDUtils.saveKnownNameAndUUID(playerName, id);
			} else {
				// Utils.debugMessage("Failed! Could not get a uuid for the
				// player
				// at all.");
				isReal = false;
				return;
			}
		}
		this.playerName = playerName;
		createUser();
	}

	public DBUser(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean isReal() {
		return isReal;
	}

	@Override
	public void createUser() {
		try {
			PostalService.getPSDatabase().updateSQL("INSERT IGNORE INTO ps_users VALUES (\"" + this.getIdentifier() + "\",\"" + this.getPlayerName() + "\",\"\")");
		} catch (Exception e) {
		}
	}

	@Override
	public String getIdentifier() {
		if (Config.USE_UUIDS) {
			return uuid.toString();
		} else {
			return playerName;
		}
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
		this.playerName = playerName;
		try {
			PostalService.getPSDatabase().updateSQL("UPDATE ps_users SET player_name = \"" + playerName + "\" WHERE player_id = \"" + getIdentifier() + "\"");
		} catch (Exception e) {
		}
	}

	@Override
	public Mail[] getInbox(WorldGroup worldGroup) {
		return null;
	}

	@Override
	public Mail[] getSentbox(WorldGroup worldGroup) {
		return null;
	}

	@Override
	public ItemStack[] getDropbox(WorldGroup worldGroup) {
		return null;
	}

	@Override
	public void saveDropbox(List<ItemStack> items, WorldGroup worldGroup) {

	}

	@Override
	public Mail[] getBoxFromType(BoxType type, WorldGroup worldGroup) {
		if (type == BoxType.INBOX) {
			return getInbox(worldGroup);
		} else {
			return getSentbox(worldGroup);
		}
	}

	@Override
	public int getUnreadMailCount(WorldGroup worldGroup) {
		return 0;
	}

	@Override
	public boolean inboxIsFull(WorldGroup worldGroup) {

		return false;
	}

	@Override
	public boolean sendMail(String recipient, String message, String attachmentData, MailType mailType, WorldGroup worldGroup) {
		// save mail to ps_mail
		// get auto incremented mail id
		UserFactory.getUser(recipient).receieveMail(0/* mailID */, Bukkit.getPlayer(getUUID()), mailType, worldGroup);
		return false;
	}

	@Override
	public boolean receieveMail(long mailID, Player sender, MailType mailType, WorldGroup worldGroup) {
		// save to ps_received. 0 for status, 0 for deleted
		// send message to user saying they have received mail
		return false;
	}

	@Override
	public boolean markAllMailAsRead(WorldGroup worldGroup) {
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
}
