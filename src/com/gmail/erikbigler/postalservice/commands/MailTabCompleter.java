package com.gmail.erikbigler.postalservice.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class MailTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAIL.toString())) {
			if(args.length == 0) {
				return null;
			}
			else if(args.length == 1) {
				List<String> mailTypeNames = new ArrayList<String>();
				for(String name : MailManager.getInstance().getMailTypeNames()) {
					mailTypeNames.add(name.toLowerCase());
				}
				return mailTypeNames;
			}
			else {

				List<String> matches = Utils.getNamesThatStartWith(args[args.length-1]);
				if(!matches.isEmpty()) return matches;

				MailType mailType = MailManager.getInstance().getMailTypeByName(args[0]);
				if(mailType != null) {
					List<String> cmdArgs = new ArrayList<String>();
					String to = Phrases.COMMAND_ARG_TO.toString() + ":";
					String message = Phrases.COMMAND_ARG_MESSAGE.toString() + ":";
					String typeArg = mailType.getAttachmentCommandArgument();
					if(!argsContainString(args, to)) {
						cmdArgs.add(to);
					}
					if(!argsContainString(args, message)) {
						cmdArgs.add(message);
					}
					if(typeArg != null && !typeArg.isEmpty()) {
						if(!argsContainString(args, typeArg + ":")) {
							cmdArgs.add(typeArg + ":");
						}
					}
					return cmdArgs;
				}
			}
		}
		return null;
	}

	private boolean argsContainString(String[] args, String string) {
		for(String arg : args) {
			if(arg.startsWith(string)) return true;
		}
		return false;
	}
}
