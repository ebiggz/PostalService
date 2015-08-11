package com.ebiggz.postalservice.permissions;

import java.util.HashMap;
import java.util.Map;

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
import org.bukkit.permissions.PermissionDefault;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.WorldGroup;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.mail.MailManager;

public class PermissionHandler {

	public enum Perm {
		MAIL_CHECK, MAIL_SELF, MAIL_READ, MAIL_READOTHER, HELP, MAILBOX_FIND, MAILBOX_SET, MAILBOX_REMOVE, MAILBOX_REMOVEALL, MAILBOX_REMOVEALLOTHER, MAILBOX_SETOVERRIDE, MAILBOX_REMOVEOTHER, OVERRIDE_WORLD_BLACKLIST, OVERRIDE_REQUIRE_MAILBOX, RELOAD, UPDATE
	}


	public static boolean playerHasPermission(Perm perm, CommandSender player, boolean notify) {
		boolean hasPerm = false;
		switch(perm) {
		case HELP:
			hasPerm = player.hasPermission("postalservice.help");
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
		case MAIL_SELF:
			hasPerm = player.hasPermission("postalservice.mail.self");
			break;
		default:
			hasPerm = false;
			break;
		}
		if(!hasPerm) {
			if(notify) player.sendMessage(Phrases.ERROR_NO_PERMISSION.toPrefixedString());
		}
		return hasPerm;
	}

	public static void registerPermissions() {
		Map<String, Boolean> userChildren = new HashMap<String, Boolean>();
		userChildren.put("postalservice.mail.read", true);
		userChildren.put("postalservice.mail.send.*", true);
		userChildren.put("postalservice.mail.check", true);
		userChildren.put("postalservice.mailbox.set", true);
		userChildren.put("postalservice.mailbox.remove", true);
		userChildren.put("postalservice.mailbox.removeall", true);
		userChildren.put("postalservice.find", true);
		userChildren.put("postalservice.help", true);
		Bukkit.getPluginManager().removePermission("postalservice.user");
		Bukkit.getPluginManager().addPermission(new Permission("postalservice.user", PermissionDefault.FALSE, userChildren));

		Map<String, Boolean> modChildren = new HashMap<String, Boolean>();
		modChildren.put("postalservice.mailbox.setoverride", true);
		modChildren.put("postalservice.mailbox.removeother", true);
		modChildren.put("postalservice.mailbox.removeallother", true);
		modChildren.put("postalservice.mail.readother", true);
		Bukkit.getPluginManager().removePermission("postalservice.mod");
		Bukkit.getPluginManager().addPermission(new Permission("postalservice.mod", PermissionDefault.FALSE, modChildren));

		Map<String, Boolean> adminChildren = new HashMap<String, Boolean>();
		adminChildren.put("postalservice.update", true);
		adminChildren.put("postalservice.reload", true);
		adminChildren.put("postalservice.overriderequiremailbox", true);
		adminChildren.put("postalservice.overrideworldblacklist", true);
		adminChildren.put("postalservice.mail.self", true);
		Bukkit.getPluginManager().removePermission("postalservice.admin");
		Bukkit.getPluginManager().addPermission(new Permission("postalservice.admin", PermissionDefault.FALSE, adminChildren));

		Map<String, Boolean> opChildren = new HashMap<String, Boolean>();
		opChildren.put("postalservice.user", true);
		opChildren.put("postalservice.mod", true);
		opChildren.put("postalservice.admin", true);
		Bukkit.getPluginManager().removePermission("postalservice.*");
		Bukkit.getPluginManager().addPermission(new Permission("postalservice.*", PermissionDefault.OP, opChildren));

		for(String perm : userChildren.keySet()) {
			Bukkit.getPluginManager().removePermission(perm);
			Bukkit.getPluginManager().addPermission(new Permission(perm, PermissionDefault.FALSE));
		}

		for(String perm : modChildren.keySet()) {
			Bukkit.getPluginManager().removePermission(perm);
			Bukkit.getPluginManager().addPermission(new Permission(perm, PermissionDefault.FALSE));
		}

		for(String perm : adminChildren.keySet()) {
			Bukkit.getPluginManager().removePermission(perm);
			Bukkit.getPluginManager().addPermission(new Permission(perm, PermissionDefault.FALSE));
		}
	}

	public static boolean playerCanMailType(String typeName, CommandSender sender) {
		return playerCanMailType(typeName, (Player) sender);
	}

	public static boolean playerCanMailType(String typeName, Player player) {
		return (player.hasPermission("postalservice.mail.send." + typeName.toLowerCase().trim()));
	}

	public static boolean playerCanMailSomething(CommandSender sender) {
		for(String typeName : MailManager.getInstance().getMailTypeNames()) {
			if(playerCanMailType(typeName, sender)) return true;
		}
		return false;
	}

	public static boolean playerHasMetMailboxLimit(Player player, WorldGroup group) {
		return (PostalService.getMailboxManager().getMailboxCount(player.getName(), group) >= Config.getMailboxLimitForPlayer(player.getName()));
	}

	public static boolean playerCanCreateMailboxAtLoc(Location loc, Player player) {
		Block block = loc.getBlock();
		Location belowOne = loc.clone();
		belowOne.setY(belowOne.getY()-1);
		int spawnRadius = Bukkit.getServer().getSpawnRadius();
		Location spawn = loc.getWorld().getSpawnLocation();
		boolean canBuild = (spawnRadius <= 0) || (player.isOp()) || (Math.max(Math.abs(block.getX() - spawn.getBlockX()), Math.abs(block.getZ() - spawn.getBlockZ())) > spawnRadius);
		BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), belowOne.getBlock(), new ItemStack(Material.CHEST), player, canBuild);
		BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(placeEvent);
		Bukkit.getPluginManager().callEvent(breakEvent);
		return (!placeEvent.isCancelled() && !breakEvent.isCancelled());
	}
}
