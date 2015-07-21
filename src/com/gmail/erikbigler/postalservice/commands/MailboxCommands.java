package com.gmail.erikbigler.postalservice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.erikbigler.postalservice.config.Language.Phrases;

public class MailboxCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAILBOX.toString()) || commandLabel.equalsIgnoreCase("mailbox")) {

		}
		return true;
	}
}
