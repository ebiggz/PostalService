package com.gmail.erikbigler.postalservice.permissions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.config.WorldGroup;

public class PermissionHandler {

	public enum Perm {
		MAIL, MAIL_CHECK, MAIL_READ, MAIL_READOTHER, HELP, MAILBOX_FIND, MAILBOX_SET, MAILBOX_REMOVE, MAILBOX_REMOVEALL, MAILBOX_REMOVEALLOTHER, MAILBOX_SETOVERRIDE, MAILBOX_REMOVEOTHER, OVERRIDE_WORLD_BLACKLIST, OVERRIDE_REQUIRE_MAILBOX, RELOAD, UPDATE
	}


	public static boolean playerHasPermission(Perm perm, CommandSender player, boolean notify) {
		boolean hasPerm = false;
		switch(perm) {
		case HELP:
			hasPerm = player.hasPermission("postalservice.help");
			break;
		case MAIL:
			hasPerm = player.hasPermission("postalservice.mail");
			break;
		case MAILBOX_FIND:
			hasPerm = player.hasPermission("postalservice.mailbox.find");
			break;
		case MAILBOX_REMOVE:
			hasPerm = player.hasPermission("postalservice.mailbox.remove");
			break;
		case MAILBOX_REMOVEALL:
			hasPerm = player.hasPermission("postalservice.mailbox.removeall");
			break;
		case MAILBOX_REMOVEALLOTHER:
			hasPerm = player.hasPermission("postalservice.mailbox.removeallother");
			break;
		case MAILBOX_REMOVEOTHER:
			hasPerm = player.hasPermission("postalservice.mailbox.removeother");
			break;
		case MAILBOX_SET:
			hasPerm = player.hasPermission("postalservice.mailbox.set");
			break;
		case MAILBOX_SETOVERRIDE:
			hasPerm = player.hasPermission("postalservice.mailbox.setoverride");
			break;
		case MAIL_CHECK:
			hasPerm = player.hasPermission("postalservice.mail.check");
			break;
		case MAIL_READOTHER:
			hasPerm = player.hasPermission("postalservice.mail.readother");
			break;
		case RELOAD:
			hasPerm = player.hasPermission("postalservice.reload");
			break;
		case UPDATE:
			hasPerm = player.hasPermission("postalservice.update");
			break;
		case OVERRIDE_REQUIRE_MAILBOX:
			hasPerm = player.hasPermission("postalservice.overriderequiremailbox");
			break;
		case OVERRIDE_WORLD_BLACKLIST:
			hasPerm = player.hasPermission("postalservice.overrideworldblacklist");
			break;
		case MAIL_READ:
			hasPerm = player.hasPermission("postalservice.mail.read");
			break;
		}
		if(!hasPerm) {
			if(notify) player.sendMessage(Phrases.ERROR_NO_PERMISSION.toPrefixedString());
		}
		return hasPerm;
	}

	public static void registerPermissions() {
		//parents
		//
		List<Permission> perms = new ArrayList<Permission>();
		//user perms
		//TODO register perms in code
		//new Permission("postalservice.mail.send.*", PermissionDefault.FALSE).;

	}

	public static boolean playerCanMailType(String typeName, CommandSender sender) {
		return playerCanMailType(typeName, (Player) sender);
	}

	public static boolean playerCanMailType(String typeName, Player player) {
		return (player.hasPermission("postalservice.mail.send." + typeName.toLowerCase()));
	}

	public static boolean playerHasMetMailboxLimit(Player player, WorldGroup group) {
		return (PostalService.getMailboxManager().getMailboxCount(player.getName(), group) >= Config.getMailboxLimitForPlayer(player.getName()));
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
