package com.ebiggz.postalservice.mailbox;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.ebiggz.postalservice.mail.Mail;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.WorldGroup;
import com.ebiggz.postalservice.events.PlayerRegisterMailboxEvent;
import com.ebiggz.postalservice.events.PlayerUnregisterAllMailboxesEvent;
import com.ebiggz.postalservice.events.PlayerUnregisterMailboxEvent;
import com.ebiggz.postalservice.exceptions.MailboxException;
import com.ebiggz.postalservice.exceptions.MailboxException.Reason;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.utils.ParticleEffect;
import com.ebiggz.postalservice.utils.Utils;

public class MailboxManager {

	public HashMap<Player, MailboxSelection> mailboxSelectors = new HashMap<Player, MailboxSelection>();
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
					Utils.debugMessage("Loading mailbox at location: " + location);
					String playerIdentifier = rs.getString("PlayerID");
					boolean isPostOffice = rs.getBoolean("IsPostOffice");
					if(location != null || playerIdentifier != null) {
						Location loc = Utils.stringToLocation(location);
						Utils.debugMessage("Converted location: " + loc.toString());
						mailboxes.put(loc, new Mailbox(loc, playerIdentifier, isPostOffice));
					} else {
						Utils.debugMessage("Could not load mailbox. Location or player id is null");
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
		Utils.debugMessage("location to add for new mailbox: " + location.getWorld());
		User user = UserFactory.getUser(player.getName());
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(((Chest) location.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
			throw new MailboxException(Reason.DOUBLE_CHEST);
		} else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		} else if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETOVERRIDE, player, false) && !PermissionHandler.playerCanCreateMailboxAtLoc(location, player)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else if(this.getMailboxCount(player.getName(), Config.getWorldGroupFromWorld(location.getWorld().getName())) >= Config.getMailboxLimitForPlayer(player.getName())) {
			throw new MailboxException(Reason.MAX_REACHED);
		} else if(!((Chest) location.getBlock().getState()).getInventory().isEmpty()) {
			throw new MailboxException(Reason.CHEST_NOT_EMPTY);
		} else {
			PlayerRegisterMailboxEvent event = new PlayerRegisterMailboxEvent(player, location);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCanceled()) {
				if(Config.USE_DATABASE) {
					try {
						Utils.debugMessage("Adding mailbox to database for location: " + Utils.locationToString(location));
						PostalService.getPSDatabase().updateSQL("INSERT INTO ps_mailboxes VALUES ('" + Utils.locationToString(location) + "', '" + user.getIdentifier() + "')");
						this.mailboxes.put(location,new Mailbox(location, user.getIdentifier(), false));
					} catch (Exception e) {
						if(Config.ENABLE_DEBUG) {
							e.printStackTrace();
						}
						Utils.debugMessage("error saving a mailbox for " + user.getPlayerName() + " at location " + Utils.locationToString(location));
						throw new MailboxException(Reason.UNKOWN);
					}
				}
			}
		}
	}

	public void addMailboxAtLocForOther(Player creator, String ownerName, Location location) throws MailboxException {
		Utils.debugMessage("location to add for new mailbox for user " + ownerName + ": " + location.getWorld());
		User user = UserFactory.getUser(ownerName);
		if(user == null) {
			throw new MailboxException(Reason.UNKOWN);
		}
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(((Chest) location.getBlock().getState()).getInventory().getHolder() instanceof DoubleChest) {
			throw new MailboxException(Reason.DOUBLE_CHEST);
		} else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		} else if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETOTHER, creator, false)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else if(!((Chest) location.getBlock().getState()).getInventory().isEmpty()) {
			throw new MailboxException(Reason.CHEST_NOT_EMPTY);
		} else {
			if(Config.USE_DATABASE) {
				try {
					Utils.debugMessage("Adding mailbox to database for location: " + Utils.locationToString(location));
					PostalService.getPSDatabase().updateSQL("INSERT INTO ps_mailboxes (Location, PlayerID) VALUES ('" + Utils.locationToString(location) + "', '" + user.getIdentifier() + "')");
					this.mailboxes.put(location,new Mailbox(location, user.getIdentifier(), false));
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG) {
						e.printStackTrace();
					}
					Utils.debugMessage("error saving a mailbox for " + user.getPlayerName() + " at location " + Utils.locationToString(location));
					throw new MailboxException(Reason.UNKOWN);
				}
			}
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
			// clear out any items used for unread notifications
			((Chest) location.getBlock().getState()).getInventory().clear();
			PlayerUnregisterMailboxEvent event = new PlayerUnregisterMailboxEvent(player, mb);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCanceled()) {
				if(Config.USE_DATABASE) {
					try {
						PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes WHERE Location = '" + Utils.locationToString(mb.getLocation()) + "'");
						this.mailboxes.remove(location);
					} catch (Exception e) {
						if(Config.ENABLE_DEBUG)
							e.printStackTrace();
					}
				}
			}
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

	public void removeAllMailboxes(Player commandSender, String owner) {
		User user = UserFactory.getUser(owner);
		PlayerUnregisterAllMailboxesEvent event = new PlayerUnregisterAllMailboxesEvent(commandSender, user);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(!event.isCanceled()) {
			if(Config.USE_DATABASE) {
				try {
					PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes WHERE PlayerID = '" + user.getIdentifier() + "'");
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG)
						e.printStackTrace();
				}
			}
			for(Location mailboxLoc : mailboxes.keySet()) {
				Mailbox mailbox = this.mailboxes.get(mailboxLoc);
				if(mailbox.getOwner().getPlayerName().equals(owner))
					mailboxes.remove(mailboxLoc);
			}
		}
	}

	public boolean toggleMailboxPostOfficeStatus(Player player, Location location) throws MailboxException {
		Mailbox mb = this.getMailbox(location);

		if(mb == null) {
			throw new MailboxException(Reason.DOESNT_EXIST);
		} else if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETPOSTOFFICE, player, false)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else {
			mb.setIsPostOffice(!mb.isPostOffice());
			if(Config.USE_DATABASE) {
				try {
					PostalService.getPSDatabase().updateSQL("UPDATE ps_mailboxes SET IsPostOffice = "
							+ (mb.isPostOffice() ? "1" : "0")
							+ " WHERE Location = '" + Utils.locationToString(mb.getLocation()) + "'");
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG)
						e.printStackTrace();
				}
			}
		}

		return mb.isPostOffice();
	}

	public void purgeAllMailboxes() {
		if(Config.USE_DATABASE) {
			try {
				PostalService.getPSDatabase().updateSQL("DELETE FROM ps_mailboxes");
			} catch (Exception e) {
				if(Config.ENABLE_DEBUG)
					e.printStackTrace();
			}
		}
		mailboxes.clear();
	}
	
	public void updateMailboxUnreadState(String playerIdentifier, boolean hasUnread) {
		
		Bukkit.getScheduler().runTask(PostalService.getPlugin(), () -> {
			
			List<Mailbox> playerMailboxes = this.mailboxes.values().stream()
					.filter(m -> m.getOwner().getIdentifier().equals(playerIdentifier))
					.collect(Collectors.toList());
		
			for(Mailbox mailbox : playerMailboxes) {
				Chest chest = mailbox.getChest();
				if(chest == null) return;
				if(hasUnread) {
					chest.getSnapshotInventory().setContents(new ItemStack[] { 
							new ItemStack(Material.DIRT, 1)
					});
				} else {
					chest.getSnapshotInventory().clear();
				}
				chest.update(true);
			}
		});
		
	}

	public int getMailboxCount(String name, WorldGroup group) {
		return mailboxes.values()
				.stream()
				.filter(mb -> mb != null &&
						!mb.isPostOffice() &&
						mb.getOwner().getPlayerName().equals(name) &&
						Config.getWorldGroupFromWorld(mb.getLocation().getWorld()).getName().equals(group.getName()))
				.collect(Collectors.toList())
				.size();
	}

	/**
	 * @return return true if a mailbox is within 6 blocks of location
	 */
	public boolean mailboxIsNearby(Location location) {
		double distance = 6;
		for(Location mailboxLoc : mailboxes.keySet()) {
			if(!location.getWorld().equals(mailboxLoc.getWorld())) continue;
			if(location.distance(mailboxLoc) < distance)
				return true;
		}
		return false;
	}

	/**
	 * @return the nearest mailbox within 5 blocks of location, null if none found
	 */
	public Mailbox getNearestMailbox(Location location) {
		double distance = 6;
		Mailbox nearest = null;
		double nearestDist = distance;
		for(Mailbox mailbox: mailboxes.values()) {
			if(!location.getWorld().equals(mailbox.getLocation().getWorld())) continue;
			double nextDist = location.distance(mailbox.getLocation());
			if(nextDist >= distance) continue;
			if(nearest == null) {
				nearest = mailbox;
				nearestDist = nextDist;
			} else {
				if(nextDist < nearestDist) {
					nearestDist = nextDist;
					nearest = mailbox;
				}
			}
		}
		return nearest;
	}
}
