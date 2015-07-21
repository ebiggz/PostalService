package com.gmail.erikbigler.postalservice.mailbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.config.ConfigAccessor;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException.Reason;


public class MailboxManager {

	public HashMap<Player,MailboxSelect> mailboxSelectors = new HashMap<Player,MailboxSelect>();
	public List<Player> willDropBook = new ArrayList<Player>();

	protected MailboxManager() { /*exists to block instantiation*/ }
	private static MailboxManager instance = null;
	public static MailboxManager getInstance() {
		if(instance == null) {
			instance = new MailboxManager();
		}
		return instance;
	}

	public enum MailboxSelect {
		ADD, REMOVE
	}


	public boolean locationHasMailbox(Location location) {
		ConfigAccessor mailboxData = new ConfigAccessor("mailbox-locations.yml");
		return mailboxData.getConfig().contains(this.locationToString(location));
	}

	public Location[] getMailboxLocations() {

		ConfigAccessor mailboxData = new ConfigAccessor("mailbox-locations.yml");
		Set<String> keys = mailboxData.getConfig().getKeys(false);
		Location[] mailboxLocs = new Location[keys.size()];

		int index = 0;
		for(String locStr : keys) {
			mailboxLocs[index] = stringToLocation(locStr);
			index++;
		}
		return mailboxLocs;
	}

	public String getMailboxOwner(Location loc) {
		return "";
	}

	public int getMailboxCount(String playerName) {
		return 0;
	}

	public void addMailboxAtLoc(Location location, Player player) throws MailboxException {
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		}
		else if(!canBreakAndPlaceAtLoc(location, player)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		}
		else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		}
		else {
			//savemailbox
		}
	}

	public void removeMailboxAtLoc(Location location, Player player) throws MailboxException {
		/*if(mythian.getMailboxLocs().size() >= getMaxMailboxCount(owner)) {
			throw new MailException(Reason.MAX_MAILBOXES_REACHED);
		}*/
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		}
		else if(!getMailboxOwner(location).equals(player.getName())) {
			throw new MailboxException(Reason.NOT_OWNER);
		}
		else if(!this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.DOESNT_EXIST);
		}
		else {
			//delete mailbox
		}
	}

	public void removeAllMailboxes(String owner) {

	}

	public boolean mailboxIsNearby(Location location, int distance) {
		Location[] mailboxes = getMailboxLocations();
		for(Location mailbox : mailboxes) {
			if(location.distance(mailbox) < distance) return true;
		}
		return false;
	}

	private boolean canBreakAndPlaceAtLoc(Location loc, Player player) {
		Block block = loc.getBlock();
		int spawnRadius = Bukkit.getServer().getSpawnRadius();
		Location spawn = loc.getWorld().getSpawnLocation();
		boolean canBuild = (spawnRadius <= 0) || (player.isOp()) || (Math.max(Math.abs(block.getX() - spawn.getBlockX()), Math.abs(block.getZ() - spawn.getBlockZ())) > spawnRadius);
		BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), null, new ItemStack(Material.CHEST), player, canBuild);
		BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(placeEvent);
		Bukkit.getPluginManager().callEvent(breakEvent);
		return (!placeEvent.isCancelled() && !breakEvent.isCancelled());
	}

	public int getMaxMailboxCount(String playerName) {
		/*String primaryGroup = Mythsentials.permission.getPrimaryGroup("", playerName);
		if(primaryGroup != null) {
			return Mythsentials.getMaxMailboxesForGroup(primaryGroup);
		}*/
		return 1;
	}

	private String locationToString(Location location) {
		return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName();
	}

	private Location stringToLocation(String string) {
		String[] split = string.split(",");
		return new Location(Bukkit.getWorld(split[3]), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
	}

}
