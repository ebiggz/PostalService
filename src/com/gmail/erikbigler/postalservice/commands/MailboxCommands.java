package com.gmail.erikbigler.postalservice.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager.MailboxSelect;

public class MailboxCommands implements CommandExecutor {

	//TODO: Add phrases for all these messages

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAILBOX.toString()) || commandLabel.equalsIgnoreCase("mailbox")) {
			if(args.length == 0) {
				//FancyMenu.showClickableCommandList(sender, commandLabel, "Mythian Postal Service", commandData, 1);
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_SET.toString())) {
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender, MailboxSelect.SET);
					//sender.sendMessage(ChatColor.YELLOW + "[Mythica] " + ChatColor.DARK_AQUA + "Please click a chest to register it as a mailbox...");
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVE.toString())) {
					MailboxManager.getInstance().mailboxSelectors.put((Player) sender, MailboxSelect.REMOVE);
					//sender.sendMessage(ChatColor.YELLOW + "[Mythica] " + ChatColor.DARK_AQUA + "Please click a chest to unregister it as a mailbox...");
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_REMOVEALL.toString())) {
					//MailboxManager.getInstance().removeAllMailboxes(sender.getName());
					//sender.sendMessage(ChatColor.YELLOW + "[Mythica] " + ChatColor.DARK_AQUA + "Unregistered all your mailboxes.");
				} else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_FIND.toString())) {
					if(MailboxManager.getInstance().markNearbyMailboxes(player)) {
						//player.sendMessage(ChatColor.YELLOW + "[MPS]" + ChatColor.DARK_AQUA + " Marking nearby mailboxes!");
					} else {
						//player.sendMessage(ChatColor.RED + "[MPS] There are no nearby mailboxes!");
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
								//player.sendMessage(ChatColor.YELLOW + "[Mythica] " + ChatColor.AQUA + "Mailbox selection timed out.");
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
