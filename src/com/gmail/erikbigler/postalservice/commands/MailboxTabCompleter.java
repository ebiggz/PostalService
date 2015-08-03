package com.gmail.erikbigler.postalservice.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class MailboxTabCompleter implements TabCompleter {

	List<String> subCommands = Arrays.asList(Phrases.COMMAND_ARG_FIND.toString(),Phrases.COMMAND_ARG_REMOVE.toString(),Phrases.COMMAND_ARG_REMOVEALL.toString(),Phrases.COMMAND_ARG_SET.toString());

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAILBOX.toString())) {
			if(args.length == 1) {
				return Utils.getAllStartsWith(args[0], subCommands);
			}
		}
		return null;
	}
}
