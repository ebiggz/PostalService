package com.gmail.erikbigler.postalservice.mailbox;

import java.sql.ResultSet;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.WorldGroup;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException.Reason;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler.Perm;
import com.gmail.erikbigler.postalservice.utils.ParticleEffect;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class MailboxManager {

	public HashMap<Player, MailboxSelect> mailboxSelectors = new HashMap<Player, MailboxSelect>();
	private HashMap<Location,Mailbox> mailboxes = new HashMap<Location,Mailbox>();

	protected MailboxManager() {
	/* exists to block instantiation */ }

	private static MailboxManager instance = null;

	public static MailboxManager getInstance() {
		if(instance == null) {
			instance = new MailboxManager();
		}
		return instance;
	}

	public enum MailboxSelect {
		SET, REMOVE
	}

	public boolean locationHasMailbox(Location location) {
		Mailbox mb = this.getMailbox(location);
		return mb != null;
	}

	public void loadMailboxes() {
		this.mailboxes.clear();
		if(Config.USE_DATABASE) {
			try {
				ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT * FROM ps_mailboxes");
				while(rs.next()) {
					String location = rs.getString("Location");
					Utils.debugMessage("Loading mailbox at locaiton: " + location);
					String playerIdentifier = rs.getString("PlayerID");
					if(location != null || playerIdentifier != null) {
						Location loc = Utils.stringToLocation(location);
						mailboxes.put(loc, new Mailbox(loc, playerIdentifier));
					}
				}
			} catch (Exception e) {
				if(Config.ENABLE_DEBUG)
					e.printStackTrace();
			}
		}
	}

	public Mailbox getMailbox(Location loc) {
		if(mailboxes.containsKey(loc)) {
			return mailboxes.get(loc);
		}
		return null;
	}

	public void addMailboxAtLoc(Location location, Player player) throws MailboxException {
		User user = UserFactory.getUser(player.getName());
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(((Chest) location.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
			throw new MailboxException(Reason.DOUBLE_CHEST);
		} else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		} else if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETOVERRIDE, player, false) && !PermissionHandler.playerCanCreateMailboxAtLoc(location, player)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else if(this.getMailboxCount(player.getName(), Config.getWorldGroupFromWorld(location.getWorld())) >= Config.getMailboxLimitForPlayer(player.getName())) {
			throw new MailboxException(Reason.MAX_REACHED);
		} else {
			if(Config.USE_DATABASE) {
				try {
					PostalService.getPSDatabase().updateSQL("INSERT IGNORE INTO ps_mailboxes VALUES (\"" + Utils.locationToString(location) + "\", \"" + user.getIdentifier() + "\")");
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG)
						e.printStackTrace();
				}
			}
			this.mailboxes.put(location,new Mailbox(location, user.getIdentifier()));
		}
	}

	public void removeMailboxAtLoc(Location location, Player player) throws MailboxException {

		Mailbox mb = this.getMailbox(location);

		if(mb == null) {
			throw new MailboxException(Reason.DOESNT_EXIST);
		} else if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(!mb.getOwner().getPlayerName().equals(player.getName()) && !PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEOTHER, player, false)) {
			throw new MailboxException(Reason.NOT_OWNER);
		} else {
			if(Config.USE_DATABASE) {
				try {
					PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes WHERE Location = \"" + Utils.locationToString(mb.getLocation()) + "\"");
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG)
						e.printStackTrace();
				}
			}
			this.mailboxes.remove(mb);
		}
	}

	public boolean markNearbyMailboxes(Player player) {
		boolean found = false;
		for(Location mailboxLoc : this.mailboxes.keySet()) {
			if(mailboxLoc.getWorld() != player.getLocation().getWorld())
				continue;
			if(player.getLocation().distance(mailboxLoc) < 20) {
				Location loc = mailboxLoc.clone();
				ParticleEffect effect = new ParticleEffect(ParticleEffect.ParticleType.VILLAGER_HAPPY, 0, 200, 0, 4, 0);
				loc.setX(loc.getX() + 0.5);
				loc.setZ(loc.getZ() + 0.5);
				loc.setY(loc.getY() + 10);
				effect.sendToLocation(loc, player);
				found = true;
			}
		}
		return found;
	}

	public void removeAllMailboxes(String owner) {
		User user = UserFactory.getUser(owner);
		if(Config.USE_DATABASE) {
			try {
				PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes WHERE PlayerID = \"" + user.getIdentifier() + "\"");
			} catch (Exception e) {
				if(Config.ENABLE_DEBUG)
					e.printStackTrace();
			}
		}
		for(int i = 0; i< this.mailboxes.size(); i++) {
			Mailbox mailbox = this.mailboxes.get(i);
			if(mailbox.getOwner().getPlayerName().equals(owner))
				mailboxes.remove(i);
		}
	}

	public int getMailboxCount(String name, WorldGroup group) {
		int count = 0;
		for(Mailbox mailbox : mailboxes.values()) {
			if(!mailbox.getOwner().getPlayerName().equals(name)) continue;
			if(Config.getWorldGroupFromWorld(mailbox.getLocation().getWorld()).getName().equals(group.getName())) {
				count++;
			}
		}
		return count;
	}

	public boolean mailboxIsNearby(Location location, int distance) {
		for(Location mailboxLoc : mailboxes.keySet()) {
			if(location.distance(mailboxLoc) < distance)
				return true;
		}
		return false;
	}
}
