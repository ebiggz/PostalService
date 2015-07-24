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

import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;

public class PermissionHandler {

	enum CommandPerm {
		MAIL, MAIL_CHECK, MAIL_CHECKOTHER, HELP, MAILBOX_FIND, MAILBOX_SET, MAILBOX_REMOVE, MAILBOX_REMOVEALL, MAILBOX_REMOVEALLOTHER, MAILBOX_SETOTHER, MAILBOX_REMOVEOTHER, RELOAD
	}

	public static boolean playerHasPermission(CommandPerm perm, CommandSender sender) {
		return playerHasPermission(perm, (Player) sender);
	}

	public static boolean playerHasPermission(CommandPerm perm, Player player) {
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
			return player.hasPermission("postalservice.mail.check.other");
		case RELOAD:
			return player.hasPermission("postalservice.reload");
		default:
			return false;
		}
	}

	public static boolean playerCanMailType(String typeName, CommandSender sender) {
		return playerCanMailType(typeName, (Player) sender);
	}

	public static boolean playerCanMailType(String typeName, Player player) {
		return (player.hasPermission("postalservice.mail.type." + typeName.toLowerCase()) || player.hasPermission("postalservice.mail.type." + typeName) || player.hasPermission("postalservice.mail.type.*"));
	}

	public static boolean playerHasMetMailboxLimit(Player player) {

		return true;
	}

	public static boolean getPlayerInboxSize(Player player) {
		User user = UserFactory.getUser(player.getName());
		//TODO: get inbox limit
		return true;
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
