package com.gmail.erikbigler.postalservice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.erikbigler.postalservice.configs.Language.Phrases;

public class Commands implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAIL.toString())) {

		}
		return true;
	}
}
