package com.gmail.erikbigler.postalservice.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager.MailboxSelect;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler.Perm;
import com.gmail.erikbigler.postalservice.utils.Utils;

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
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_SET, sender, true)) return true;
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender, MailboxSelect.SET);
					sender.sendMessage(Phrases.ALERT_MAILBOX_SET.toPrefixedString());
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVE.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVE, sender, true)) return true;
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender, MailboxSelect.REMOVE);
					sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE.toPrefixedString());
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVEALL.toString())) {
					if(args.length == 1) {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALL, sender, true)) return true;
						MailboxManager.getInstance().removeAllMailboxes(sender.getName());
						sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE_ALL.toPrefixedString());
					} else {
						if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALLOTHER, sender, true)) return true;
						String completedName = Utils.completeName(args[1]);
						if(completedName == null || completedName.isEmpty()) {
							completedName = args[1];
						}
						MailboxManager.getInstance().removeAllMailboxes(completedName);
						sender.sendMessage(Phrases.ALERT_MAILBOX_REMOVE_ALL_OTHER.toPrefixedString().replace("%player%", completedName));
					}
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_FIND.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.MAILBOX_FIND, sender, true)) return true;
					if(MailboxManager.getInstance().markNearbyMailboxes(player)) {
						sender.sendMessage(Phrases.ALERT_MAILBOX_FIND.toPrefixedString());
					} else {
						sender.sendMessage(Phrases.ERROR_MAILBOX_FIND_NONE.toPrefixedString());
					}
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
