package com.gmail.erikbigler.postalservice.permissions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.WorldGroup;

public class PermissionHandler {

	public enum Perm {
		MAIL, MAIL_CHECK, MAIL_CHECKOTHER, HELP, MAILBOX_FIND, MAILBOX_SET, MAILBOX_REMOVE, MAILBOX_REMOVEALL, MAILBOX_REMOVEALLOTHER, MAILBOX_SETOTHER, MAILBOX_REMOVEOTHER, RELOAD, OVERRIDE_WORLD_BLACKLIST, OVERRIDE_NEARBY_MAILBOX
	}

	public static boolean playerHasPermission(Perm perm, CommandSender sender) {
		return playerHasPermission(perm, (Player) sender);
	}

	public static boolean playerHasPermission(Perm perm, Player player) {
		switch(perm) {
		case HELP:
			return player.hasPermission("postalservice.help");
		case MAIL:
			return player.hasPermission("postalservice.mail");
		case MAILBOX_FIND:
			return player.hasPermission("postalservice.mailbox.find");
		case MAILBOX_REMOVE:
			return player.hasPermission("postalservice.mailbox.remove");
		case MAILBOX_REMOVEALL:
			return player.hasPermission("postalservice.mailbox.removeall");
		case MAILBOX_REMOVEALLOTHER:
			return player.hasPermission("postalservice.mailbox.removeallother");
		case MAILBOX_REMOVEOTHER:
			return player.hasPermission("postalservice.mailbox.removeother");
		case MAILBOX_SET:
			return player.hasPermission("postalservice.mailbox.set");
		case MAILBOX_SETOTHER:
			return player.hasPermission("postalservice.mailbox.setother");
		case MAIL_CHECK:
			return player.hasPermission("postalservice.mail.check");
		case MAIL_CHECKOTHER:
			return player.hasPermission("postalservice.mail.checkother");
		case RELOAD:
			return player.hasPermission("postalservice.reload");
		case OVERRIDE_NEARBY_MAILBOX:
			return player.hasPermission("postalservice.overridenearbymailbox");
		case OVERRIDE_WORLD_BLACKLIST:
			return player.hasPermission("postalservice.overrideworldblacklist");
		}
		return false;
	}

	public static boolean playerCanMailType(String typeName, CommandSender sender) {
		return playerCanMailType(typeName, (Player) sender);
	}

	public static boolean playerCanMailType(String typeName, Player player) {
		return (player.hasPermission("postalservice.mail.type." + typeName.toLowerCase()) || player.hasPermission("postalservice.mail.type." + typeName) || player.hasPermission("postalservice.mail.type.*"));
	}

	public static boolean playerHasMetMailboxLimit(Player player, WorldGroup group) {
		return (PostalService.getMailboxManager().getMailboxCount(player, group) >= Config.getMailboxLimitForPlayer(player.getName()));
	}

	public static boolean playerCanCreateMailboxAtLoc(Location loc, Player player) {
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
}
