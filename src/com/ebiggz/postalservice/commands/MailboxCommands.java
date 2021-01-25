package com.ebiggz.postalservice.commands;

import com.ebiggz.postalservice.mailbox.MailboxSelection;
import com.ebiggz.postalservice.mailbox.MailboxSelectionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.mailbox.MailboxManager;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.utils.Utils;

public class MailboxCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(!Config.ENABLE_MAILBOXES) return true;

		if(!(sender instanceof Player)) {
			sender.sendMessage(Phrases.ERROR_CONSOLE_COMMAND.toString());
			return true;
		}

		Player player = (Player) sender;

		if(Config.playerIsInBlacklistedWorld(player)) {
			if(!PermissionHandler.playerHasPermission(Perm.OVERRIDE_WORLD_BLACKLIST, sender, false)) {
				sender.sendMessage(Phrases.ERROR_BLACKLISTED_WORLD.toPrefixedString());
				return true;
			} else {
				sender.sendMessage(Phrases.ALERT_BLACKLISTED_WORLD_OVERRIDE.toPrefixedString());
			}
		}


		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAILBOX.toString()) || commandLabel.equalsIgnoreCase("mailbox") || commandLabel.equalsIgnoreCase("mb")) {
			if(args.length == 0) {
				Utils.fancyHelpMenu(sender, Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_HELP.toString()).sendPage(1, sender);
			} else if(args.length >= 1) {
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_SET.toString())) {
					if(args.length == 1) {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SET, sender, true)) return true;
						MailboxManager.getInstance().mailboxSelectors.put((Player) sender,
								new MailboxSelection(sender.getName(), MailboxSelectionType.SET));
						sender.sendMessage(Phrases.ALERT_MAILBOX_SET.toPrefixedString());
					} else {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETOTHER, sender, true)) return true;
						String completedName = Utils.completeName(args[1]);
						if(completedName == null || completedName.isEmpty()) {
							completedName = args[1];
						}
						MailboxManager.getInstance().mailboxSelectors.put((Player) sender,
								new MailboxSelection(completedName, MailboxSelectionType.SET_OTHER));
						sender.sendMessage(ChatColor.AQUA + "Please click a chest to register it as a mailbox for " + completedName + " ...");
					}
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVE.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVE, sender, true)) return true;
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender,
							new MailboxSelection(sender.getName(), MailboxSelectionType.REMOVE));
					sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE.toPrefixedString());
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVEALL.toString())) {
					if(args.length == 1) {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALL, sender, true)) return true;
						MailboxManager.getInstance().removeAllMailboxes((Player) sender, sender.getName());
						sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE_ALL.toPrefixedString());
					} else {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALLOTHER, sender, true)) return true;
						String completedName = Utils.completeName(args[1]);
						if(completedName == null || completedName.isEmpty()) {
							completedName = args[1];
						}
						MailboxManager.getInstance().removeAllMailboxes((Player) sender, completedName);
						sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE_ALL_OTHER.toPrefixedString().replace("%player%", completedName));
					}
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_FIND.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_FIND, sender, true)) return true;
					if(MailboxManager.getInstance().markNearbyMailboxes(player)) {
						sender.sendMessage(Phrases.ALERT_MAILBOX_FIND.toPrefixedString());
					} else {
						sender.sendMessage(Phrases.ERROR_MAILBOX_FIND_NONE.toPrefixedString());
					}
				} else if(args[0].equalsIgnoreCase("setpostoffice")) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SETPOSTOFFICE, sender, true)) return true;
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender,
							new MailboxSelection(sender.getName(), MailboxSelectionType.MARK_POST_OFFICE));
					sender.sendMessage(ChatColor.AQUA + "Please click a mailbox to toggle Post Office status");
				} else if(args[0].equalsIgnoreCase("purgeall")) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_PURGEALL, sender, true)) return true;
					MailboxManager.getInstance().purgeAllMailboxes();
					sender.sendMessage(ChatColor.AQUA + "Unregistered ALL mailboxes.");
				}

				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_SET.toString()) || args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVE.toString())) {
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(PostalService.getPlugin(), new Runnable() {
						private Player player;

						@Override
						public void run() {
							if(MailboxManager.getInstance().mailboxSelectors.containsKey(player)) {
								MailboxManager.getInstance().mailboxSelectors.remove(player);
								player.sendMessage(Phrases.ALERT_MAILBOX_TIMEOUT.toPrefixedString());
							}
						}

						private Runnable init(Player player) {
							this.player = player;
							return this;
						}
					}.init(player), 100L);
				}
			}
		}
		return true;
	}
}
