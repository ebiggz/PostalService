package com.gmail.erikbigler.postalservice.backend;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.config.WorldGroup;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.UUIDUtils;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class DBUser implements User {

	private UUID uuid;
	private String playerName;
	private boolean isReal = true;

	public DBUser(String playerName) {
		this.playerName = playerName;
		if (Config.USE_UUIDS) {
			UUID id = UUIDUtils.findUUID(playerName);
			if (id != null) {
				Utils.debugMessage("Success!");
				this.uuid = id;
				UUIDUtils.saveKnownNameAndUUID(playerName, id);
			} else {
				Utils.debugMessage("Failed! Could not get a uuid for the player at all.");
				isReal = false;
				return;
			}
		}
		createUser();
	}

	public DBUser(UUID uuid) {
		this.uuid = uuid;
		this.playerName = UUIDUtils.findPlayerName(uuid);
		createUser();
	}

	@Override
	public boolean isReal() {
		return isReal;
	}

	@Override
	public void createUser() {
		try {
			PostalService.getPSDatabase().updateSQL("INSERT IGNORE INTO ps_users VALUES (\"" + this.getIdentifier() + "\",\"" + this.getPlayerName() + "\", \"\")");
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
		if(playerName == null) {
			this.playerName = UUIDUtils.findPlayerName(uuid);
		}
		return playerName;
	}

	@Override
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
		try {
			PostalService.getPSDatabase().updateSQL("UPDATE ps_users SET PlayerName = \"" + playerName + "\" WHERE PlayerID = \"" + getIdentifier() + "\"");
		} catch (Exception e) {
		}
	}

	@Override
	public List<Mail> getInbox(WorldGroup worldGroup) {
		return this.queryDBByType(BoxType.INBOX, worldGroup);
	}

	@Override
	public List<Mail> getSentbox(WorldGroup worldGroup) {
		return this.queryDBByType(BoxType.SENT, worldGroup);
	}

	private List<Mail> queryDBByType(BoxType type, WorldGroup worldGroup) {
		StringBuilder query = new StringBuilder();
		if (type == BoxType.INBOX) {
			query.append("SELECT Sent.MailID, Received.ReceivedID, Sent.MailType, Sent.Message, Sent.Attachments, Sent.TimeStamp, Sender.PlayerName AS Sender, Recipient.PlayerName AS Recipient, Received.Status FROM ps_received AS Received JOIN ps_mail AS Sent ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Received.RecipientID = \"" + this.getIdentifier() + "\" AND Received.Deleted = 0");
		} else {
			query.append("SELECT Sent.MailID, Received.ReceivedID, Sent.MailType, Sent.Message, Sent.Attachments, Sent.TimeStamp, Sender.PlayerName AS Sender, Recipient.PlayerName AS Recipient, Received.Status FROM ps_mail AS Sent JOIN ps_received AS Received ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Sent.SenderID = \"" + this.getIdentifier() + "\" AND Sent.Deleted = 0");
		}
		if (Config.ENABLE_WORLD_GROUPS) {
			if (Config.containsMailTypesThatIgnoreWorldGroups()) {
				query.append(" AND (Sent.WorldGroup = \"" + worldGroup.getName() + "\" OR (");
				int remaining = Config.getMailTypesThatIgnoreWorldGroups().size();
				for (String mailType : Config.getMailTypesThatIgnoreWorldGroups()) {
					query.append("MailType = \"" + mailType.toLowerCase() + "\"");
					remaining--;
					if (remaining > 0) {
						query.append(" OR ");
					}
				}
				query.append("))");
			} else {
				query.append(" AND Sent.WorldGroup = \"" + worldGroup.getName() + "\"");
			}
		}
		query.append(" ORDER BY Sent.TimeStamp DESC LIMIT 100");
		List<Mail> sentMail = new ArrayList<Mail>();
		try {
			// Build list of mail
			MailManager mm = PostalService.getMailManager();
			ResultSet rs = PostalService.getPSDatabase().querySQL(query.toString());
			while (rs.next()) {
				MailType mailType = mm.getMailTypeByIdentifier(rs.getString("MailType"));
				if (mailType == null) {
					continue;
				}
				sentMail.add(new Mail(rs.getLong("MailID"), rs.getLong("ReceivedID"), rs.getString("Sender"), rs.getString("Recipient"), rs.getString("Message"), rs.getString("Attachments"), mailType, rs.getTimestamp("TimeStamp"), mm.getMailStatusFromID(rs.getInt("Status"))));
			}
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return sentMail;
	}

	@Override
	public List<ItemStack> getDropbox(WorldGroup worldGroup) {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT Contents FROM ps_dropboxes WHERE PlayerID = \"" + this.getIdentifier() + "\" AND WorldGroup = \"" + worldGroup.getName() + "\"");
			if (rs.next()) {
				return Utils.bytesToItems(rs.getBytes("Contents"));
			} else {
				return null;
			}
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return null;
	}

	@Override
	public void saveDropbox(List<ItemStack> items, WorldGroup worldGroup) {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT DropboxID FROM ps_dropboxes WHERE PlayerID = \"" + this.getIdentifier() + "\" AND WorldGroup = \"" + worldGroup.getName() + "\"");
			PreparedStatement statement;
			if (rs.next()) {
				statement = PostalService.getPSDatabase().getConnection().prepareStatement("UPDATE ps_dropboxes SET Contents = ? WHERE DropboxID = " + rs.getInt("DropboxID"));
			} else {
				statement = PostalService.getPSDatabase().getConnection().prepareStatement("INSERT IGNORE INTO ps_dropboxes VALUES (0,?,\"" + this.getIdentifier() + "\",\"" + worldGroup.getName() + "\")");
			}
			statement.setBytes(1, Utils.itemsToBytes(items));
			statement.execute();
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
	}

	@Override
	public List<Mail> getBoxFromType(BoxType type, WorldGroup worldGroup) {
		if (type == BoxType.INBOX) {
			return getInbox(worldGroup);
		} else {
			return getSentbox(worldGroup);
		}
	}

	@Override
	public int getUnreadMailCount(WorldGroup worldGroup) {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT count(ReceivedID) AS UnreadCount FROM ps_received AS Received JOIN ps_mail AS Sent ON Received.MailID = Sent.MailID WHERE Received.RecipientID = \"" + this.getIdentifier() + "\" AND Received.Deleted = 0 AND Received.Status = 0 AND Sent.WorldGroup = \"" + worldGroup.getName() + "\"");
			rs.next();
			return rs.getInt("UnreadCount");
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean inboxIsFull(WorldGroup worldGroup) {
		return (getBoxSizeFromType(BoxType.INBOX, worldGroup) >= Config.getMaxInboxSizeForPlayer(playerName));
	}

	@Override
	public int getBoxSizeFromType(BoxType type, WorldGroup worldGroup) {
		try {
			StringBuilder query = new StringBuilder();
			if (type == BoxType.INBOX) {
				query.append("SELECT count(Received.ReceivedID) as Size FROM ps_received AS Received JOIN ps_mail AS Sent ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Received.RecipientID = \"" + this.getIdentifier() + "\" AND Received.Deleted = 0");
			} else {
				query.append("SELECT count(Sent.MailID) as Size FROM ps_mail AS Sent JOIN ps_received AS Received ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Sent.SenderID = \"" + this.getIdentifier() + "\" AND Sent.Deleted = 0");
			}
			if (Config.ENABLE_WORLD_GROUPS) {
				if (Config.containsMailTypesThatIgnoreWorldGroups()) {
					query.append(" AND (Sent.WorldGroup = \"" + worldGroup.getName() + "\" OR (");
					int remaining = Config.getMailTypesThatIgnoreWorldGroups().size();
					for (String mailType : Config.getMailTypesThatIgnoreWorldGroups()) {
						query.append("MailType = \"" + mailType.toLowerCase() + "\"");
						remaining--;
						if (remaining > 0) {
							query.append(" OR ");
						}
					}
					query.append("))");
				} else {
					query.append(" AND Sent.WorldGroup = \"" + worldGroup.getName() + "\"");
				}
			}

			ResultSet rs = PostalService.getPSDatabase().querySQL(query.toString());
			if(rs.next()) {
				return rs.getInt("Size");
			}

		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}

		return 0;
	}

	@Override
	public boolean sendMail(String recipient, String message, String attachmentData, MailType mailType, WorldGroup worldGroup) {
		try {
			PostalService.getPSDatabase().updateSQL("INSERT INTO ps_mail VALUES (0,\"" + mailType.getIdentifier().toLowerCase() + "\",\"" + message + "\",\"" + attachmentData + "\", now(), \"" + getIdentifier() + "\", 0, \"" + worldGroup.getName() + "\")");
			UserFactory.getUser(recipient).receieveMail(Bukkit.getPlayer(getUUID()), mailType);
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean receieveMail(Player sender, MailType mailType) {
		try {
			PostalService.getPSDatabase().updateSQL("INSERT INTO ps_received VALUES (0,\"" + this.getIdentifier() + "\",LAST_INSERT_ID(), 0, 0)");
			Utils.messagePlayerIfOnline(this.getIdentifier(), Phrases.ALERT_RECEIVED_MAIL.toPrefixedString().replace("%sender%", sender.getName()));
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean markAllMailAsRead(WorldGroup worldGroup) {
		try {
			PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received JOIN ps_mail AS Sent ON Received.MailID = Sent.MailID SET Received.Status = 1 WHERE Received.RecipientID = \"" + this.getIdentifier() + "\" AND Received.Status = 0 AND Sent.WorldGroup = \"" + worldGroup.getName() + "\"");
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean markMailAsClaimed(Mail mail) {
		try {
			if (!mail.getSender().equals(playerName)) {
				PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received SET Received.Status = 2 WHERE Received.ReceivedID = " + mail.getReceivedID());
				return true;
			}
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean markMailAsDeleted(Mail mail) {
		try {
			if (mail.getSender().equals(playerName)) {
				PostalService.getPSDatabase().updateSQL("UPDATE ps_mail AS Sent SET Sent.Deleted = 1 WHERE Sent.MailID = " + mail.getMailID());
			} else {
				PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received SET Received.Deleted = 1 WHERE Received.ReceivedID = " + mail.getReceivedID());
			}
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getTimeZone() {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT TimeZone FROM ps_users WHERE PlayerID = \"" + this.getIdentifier() + "\"");
			if(rs != null && rs.next()) {
				String timezone = rs.getString("TimeZone");
				if(timezone != null && !timezone.equalsIgnoreCase("null")) return timezone;
			}
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
		return "";
	}

	@Override
	public void setTimeZone(String timezone) {
		try {
			PostalService.getPSDatabase().updateSQL("UPDATE ps_users SET TimeZone = \"" + timezone + "\" WHERE PlayerID = \"" + this.getIdentifier() + "\"");
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
	}
}
