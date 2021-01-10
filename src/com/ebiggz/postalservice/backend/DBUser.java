package com.ebiggz.postalservice.backend;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.WorldGroup;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.events.PlayerSendMailEvent;
import com.ebiggz.postalservice.mail.Mail;
import com.ebiggz.postalservice.mail.MailManager;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.mail.MailManager.BoxType;
import com.ebiggz.postalservice.utils.UUIDUtils;
import com.ebiggz.postalservice.utils.Utils;

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
			PostalService.getPSDatabase().updateSQL("INSERT IGNORE INTO ps_users VALUES ('" + this.getIdentifier() + "','" + this.getPlayerName() + "', '')");
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
			PostalService.getPSDatabase().updateSQL("UPDATE ps_users SET PlayerName = '" + playerName + "' WHERE PlayerID = '" + getIdentifier() + "'");
		} catch (Exception e) {
		}
	}

	@Override
	public List<Mail> getInbox() {
		return this.queryDBByType(BoxType.INBOX);
	}

	@Override
	public List<Mail> getSentbox() {
		return this.queryDBByType(BoxType.SENT);
	}

	private List<Mail> queryDBByType(BoxType type) {
		StringBuilder query = new StringBuilder();
		if (type == BoxType.INBOX) {
			query.append("SELECT Sent.MailID, Received.ReceivedID, Sent.MailType, Sent.Message, Sent.Attachments, Sent.TimeStamp, Sent.WorldGroup, Sender.PlayerName AS Sender, Recipient.PlayerName AS Recipient, Received.Status FROM ps_received AS Received JOIN ps_mail AS Sent ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Received.RecipientID = '" + this.getIdentifier() + "' AND Received.Deleted = 0");
		} else {
			query.append("SELECT Sent.MailID, Received.ReceivedID, Sent.MailType, Sent.Message, Sent.Attachments, Sent.TimeStamp, Sent.WorldGroup, Sender.PlayerName AS Sender, Recipient.PlayerName AS Recipient, Received.Status FROM ps_mail AS Sent JOIN ps_received AS Received ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Sent.SenderID = '" + this.getIdentifier() + "' AND Sent.Deleted = 0");
		}
		query.append(" ORDER BY Sent.TimeStamp DESC");
		if((type == BoxType.INBOX && !Config.HARD_ENFORCE_INBOX_LIMIT) || type == BoxType.SENT) {
			query.append(" LIMIT " + Config.getMaxInboxSizeForPlayer(playerName));
		}
		List<Mail> mail = new ArrayList<Mail>();
		try {
			// Build list of mail
			MailManager mm = PostalService.getMailManager();
			ResultSet rs = PostalService.getPSDatabase().querySQL(query.toString());
			while (rs.next()) {
				MailType mailType = mm.getMailTypeByIdentifier(rs.getString("MailType"));
				if (mailType == null) {
					continue;
				}
				mail.add(new Mail(rs.getLong("MailID"), rs.getLong("ReceivedID"), rs.getString("Sender"), rs.getString("Recipient"), rs.getString("Message"), rs.getString("Attachments"), mailType, rs.getTimestamp("TimeStamp"), mm.getMailStatusFromID(rs.getInt("Status")), Config.getWorldGroupFromGroupName(rs.getString("WorldGroup"))));
			}
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return mail;
	}

	@Override
	public List<ItemStack> getDropbox(WorldGroup worldGroup) {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT Contents FROM ps_dropboxes WHERE PlayerID = '" + this.getIdentifier() + "' AND WorldGroup = '" + worldGroup.getName() + "'");
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
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT DropboxID FROM ps_dropboxes WHERE PlayerID = '" + this.getIdentifier() + "' AND WorldGroup = '" + worldGroup.getName() + "'");
			PreparedStatement statement;
			if (rs.next()) {
				statement = PostalService.getPSDatabase().getConnection().prepareStatement("UPDATE ps_dropboxes SET Contents = ? WHERE DropboxID = " + rs.getInt("DropboxID"));
			} else {
				statement = PostalService.getPSDatabase().getConnection().prepareStatement("INSERT IGNORE INTO ps_dropboxes VALUES (0,?,'" + this.getIdentifier() + "','" + worldGroup.getName() + "')");
			}
			statement.setBytes(1, Utils.itemsToBytes(items));
			statement.execute();
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
	}

	@Override
	public List<Mail> getBoxFromType(BoxType type) {
		if (type == BoxType.INBOX) {
			return getInbox();
		} else {
			return getSentbox();
		}
	}

	@Override
	public int getUnreadMailCount() {
		try {
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT count(ReceivedID) AS UnreadCount FROM ps_received AS Received JOIN ps_mail AS Sent ON Received.MailID = Sent.MailID WHERE Received.RecipientID = '" + this.getIdentifier() + "' AND Received.Deleted = 0 AND Received.Status = 0");
			rs.next();
			return rs.getInt("UnreadCount");
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean inboxIsFull() {
		return (getBoxSizeFromType(BoxType.INBOX) >= Config.getMaxInboxSizeForPlayer(playerName));
	}

	@Override
	public int getBoxSizeFromType(BoxType type) {
		try {
			StringBuilder query = new StringBuilder();
			if (type == BoxType.INBOX) {
				query.append("SELECT count(Received.ReceivedID) as Size FROM ps_received AS Received JOIN ps_mail AS Sent ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Received.RecipientID = '" + this.getIdentifier() + "' AND Received.Deleted = 0");
			} else {
				query.append("SELECT count(Sent.MailID) as Size FROM ps_mail AS Sent JOIN ps_received AS Received ON Sent.MailID = Received.MailID JOIN ps_users AS Sender ON Sent.SenderID = Sender.PlayerID JOIN ps_users AS Recipient ON Received.RecipientID = Recipient.PlayerID WHERE Sent.SenderID = '" + this.getIdentifier() + "' AND Sent.Deleted = 0");
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
			User recipientUser = UserFactory.getUser(recipient);
			Player sender = Utils.getPlayerFromIdentifier(getIdentifier());
			if(Config.HARD_ENFORCE_INBOX_LIMIT && recipientUser.inboxIsFull()) {
				sender.sendMessage(Phrases.ERROR_INBOX_FULL.toPrefixedString().replace("%recipient%", recipient));
				return false;
			}

			PlayerSendMailEvent event = new PlayerSendMailEvent(this, recipientUser, message, attachmentData, mailType, worldGroup);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				sender.sendMessage(Phrases.ALERT_SENT_MAIL.toPrefixedString().replace("%mailtype%", mailType.getDisplayName()).replace("%recipient%", recipient));
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.runTaskAsynchronously(PostalService.getPlugin(), new Runnable() {
					private PlayerSendMailEvent event;
					private User recipientUser;
					@Override
					public void run() {
						try {
							PostalService.getPSDatabase().updateSQL("INSERT INTO ps_mail VALUES (0,'" + event.getMailType().getIdentifier().toLowerCase() + "','" + event.getMessage() + "','" + event.getAttachmentData() + "', now(), '" + event.getSender().getIdentifier() + "', 0, '" + event.getWorldGroup().getName() + "')");
							recipientUser.receieveMail(Utils.getPlayerFromIdentifier(event.getSender().getIdentifier()), event.getMailType());
						} catch (Exception e) {
							if(Config.ENABLE_DEBUG) e.printStackTrace();
						}
					}
					public Runnable init(PlayerSendMailEvent event, User recipientUser) {
						this.event = event;
						this.recipientUser = recipientUser;
						return this;
					}
				}.init(event, recipientUser));
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean receieveMail(Player sender, MailType mailType) {
		try {
			PostalService.getPSDatabase().updateSQL("INSERT INTO ps_received VALUES (0,'" + this.getIdentifier() + "',LAST_INSERT_ID(), 0, 0)");
			if(Config.UNREAD_NOTIFICATION_ON_RECEIVE) {
				Utils.messagePlayerIfOnline(this.getIdentifier(), Phrases.ALERT_RECEIVED_MAIL.toPrefixedString().replace("%sender%", sender.getName()));
			}
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean markAllMailAsRead() {
		try {
			PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received JOIN ps_mail AS Sent ON Received.MailID = Sent.MailID SET Received.Status = 1 WHERE Received.RecipientID = '" + this.getIdentifier() + "' AND Received.Status = 0	");
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
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.runTaskAsynchronously(PostalService.getPlugin(), new Runnable() {
				private Mail mail;
				@Override
				public void run() {
					try {
						PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received SET Received.Status = 2 WHERE Received.ReceivedID = " + mail.getReceivedID());
					} catch (Exception e) {
						if(Config.ENABLE_DEBUG) e.printStackTrace();
					}
				}
				public Runnable init(Mail mail) {
					this.mail = mail;
					return this;
				}
			}.init(mail));
			return true;
		} catch (Exception e) {
			if (Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean markMailAsDeleted(Mail mail, BoxType type) {
		try {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.runTaskAsynchronously(PostalService.getPlugin(), new Runnable() {
				private Mail mail;
				private BoxType type;
				@Override
				public void run() {
					try {
						if (type == BoxType.SENT) {
							PostalService.getPSDatabase().updateSQL("UPDATE ps_mail AS Sent SET Sent.Deleted = 1 WHERE Sent.MailID = " + mail.getMailID());
						} else {
							PostalService.getPSDatabase().updateSQL("UPDATE ps_received AS Received SET Received.Deleted = 1 WHERE Received.ReceivedID = " + mail.getReceivedID());
						}
					} catch (Exception e) {
						if(Config.ENABLE_DEBUG) e.printStackTrace();
					}
				}
				public Runnable init(Mail mail, BoxType type) {
					this.mail = mail;
					this.type = type;
					return this;
				}
			}.init(mail, type));
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
			ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT TimeZone FROM ps_users WHERE PlayerID = '" + this.getIdentifier() + "'");
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
			PostalService.getPSDatabase().updateSQL("UPDATE ps_users SET TimeZone = '" + timezone + "' WHERE PlayerID = '" + this.getIdentifier() + "'");
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
	}
}
