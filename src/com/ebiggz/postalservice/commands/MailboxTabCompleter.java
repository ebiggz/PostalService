package com.ebiggz.postalservice.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.utils.Utils;

public class MailboxTabCompleter implements TabCompleter {

	List<String> subCommands = Arrays.asList(
			Phrases.COMMAND_ARG_FIND.toString(),
			Phrases.COMMAND_ARG_REMOVE.toString(),
			Phrases.COMMAND_ARG_REMOVEALL.toString(),
			Phrases.COMMAND_ARG_SET.toString(),
			"setpostoffice",
			"purgeall"
	);

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAILBOX.toString())) {
			if(args.length == 1) {
				return Utils.getAllStartsWith(args[0], subCommands);
			} else if (args.length == 2) {
				return Stream.concat(
						Bukkit.getOnlinePlayers().stream().map(p -> p.getName()),
						Arrays.stream(Bukkit.getOfflinePlayers()).map(p -> p.getName())
				).collect(Collectors.toList());
			}
		}
		return null;
	}
}
