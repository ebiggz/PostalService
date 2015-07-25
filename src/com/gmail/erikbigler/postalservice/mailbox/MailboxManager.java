package com.gmail.erikbigler.postalservice.mailbox;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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
	private List<Mailbox> mailboxes = new ArrayList<Mailbox>();

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
					String playerIdentifier = rs.getString("PlayerID");
					if(location != null || playerIdentifier != null) {
						mailboxes.add(new Mailbox(Utils.stringToLocation(location), UserFactory.getUserFromIdentifier(playerIdentifier)));
					}
				}
			} catch (Exception e) {
				if(Config.ENABLE_DEBUG)
					e.printStackTrace();
			}
		}
	}

	public Mailbox getMailbox(Location loc) {
		for(Mailbox mailbox : mailboxes) {
			if(mailbox.getLocation().equals(loc))
				return mailbox;
		}
		return null;
	}

	public void addMailboxAtLoc(Location location, Player player) throws MailboxException {
		User user = UserFactory.getUser(player.getName());
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(!PermissionHandler.playerCanCreateMailboxAtLoc(location, player)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		} else if(this.getMailboxCount(player, Config.getWorldGroupFromWorld(location.getWorld())) >= Config.getMailboxLimitForPlayer(player.getName())) {
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
			this.mailboxes.add(new Mailbox(location, user));
		}
	}

	public void removeMailboxAtLoc(Location location, Player player) throws MailboxException {

		Mailbox mb = this.getMailbox(location);

		if(mb == null) {
			throw new MailboxException(Reason.DOESNT_EXIST);
		} else if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(!mb.getOwner().getPlayerName().equals(player.getName()) && !PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEOTHER, player)) {
			throw new MailboxException(Reason.NOT_OWNER);
		} else {
			if(Config.USE_DATABASE) {
				try {
					PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes WHERE Location = " + Utils.locationToString(mb.getLocation()));
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
		for(Mailbox mailbox : this.mailboxes) {
			if(mailbox.getLocation().getWorld() != player.getLocation().getWorld())
				continue;
			if(player.getLocation().distance(mailbox.getLocation()) < 20) {
				Location loc = mailbox.getLocation().clone();
				ParticleEffect effect = new ParticleEffect(ParticleEffect.ParticleType.VILLAGER_HAPPY, 0, 100, 0, 3, 0);
				loc.setX(loc.getX() + 0.5);
				loc.setZ(loc.getZ() + 0.5);
				loc.setY(loc.getY() + 4);
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
		for(Mailbox mailbox : this.mailboxes) {
			if(mailbox.getOwner().getPlayerName().equals(owner))
				mailboxes.remove(mailbox);
		}
	}

	public int getMailboxCount(Player player, WorldGroup group) {
		int count = 0;
		for(Mailbox mailbox : this.mailboxes) {
			if(Config.getWorldGroupFromWorld(mailbox.getLocation().getWorld()).getName().equals(group.getName())) {
				count++;
			}
		}
		return count;
	}

	public boolean mailboxIsNearby(Location location, int distance) {
		for(Mailbox mailbox : mailboxes) {
			if(location.distance(mailbox.getLocation()) < distance)
				return true;
		}
		return false;
	}
}
