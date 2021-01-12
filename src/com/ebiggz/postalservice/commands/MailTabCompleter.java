package com.ebiggz.postalservice.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.mail.MailManager;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.utils.Utils;

public class MailTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if(commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAIL.toString())) {
			if(args.length == 0) {
				return null;
			}
			else if(args.length == 1) {
				List<String> arg1List = new ArrayList<String>();
				for(String name : MailManager.getInstance().getMailTypeNames()) {
					if(!PermissionHandler.playerCanMailType(name, sender)) continue;
					arg1List.add(name.toLowerCase());
				}
				if(PermissionHandler.playerHasPermission(Perm.MAIL_CHECK, sender, false)) {
					arg1List.add(Phrases.COMMAND_ARG_CHECK.toString());
				}
				if(PermissionHandler.playerHasPermission(Perm.MAIL_CHECK, sender, false)) {
					arg1List.add(Phrases.COMMAND_ARG_HELP.toString());
				}

				arg1List.add(Phrases.COMMAND_ARG_TIMEZONE.toString());
				return Utils.getAllStartsWith(args[0], arg1List);
			}
			else {

				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_TIMEZONE.toString())) {
					return Utils.getAllStartsWith(args[1], Config.TIMEZONES);
				}

				List<String> matches = Utils.getNamesThatStartWith(args[args.length-1]);
				if(!matches.isEmpty()) return matches;

				MailType mailType = MailManager.getInstance().getMailTypeByName(args[0]);
				if(mailType != null) {
					List<String> cmdArgs = new ArrayList<String>();
					String to = Phrases.COMMAND_ARG_TO.toString() + ":";
					String message = Phrases.COMMAND_ARG_MESSAGE.toString() + ":";
					String typeArg = mailType.getAttachmentCommandArgument();
					String lastArg = args[args.length - 1].toLowerCase();
					if(!argsContainString(args, to)) {
						cmdArgs.add(to);
					}
					if(lastArg.startsWith(to)) {
						Bukkit.getOnlinePlayers().forEach(p -> {
							if(p.getName() != sender.getName()) {
								cmdArgs.add(to + p.getName());
							}
							for(OfflinePlayer offlinePlayer: Bukkit.getOfflinePlayers()) {
								String name = offlinePlayer.getName();
								if(name != null) {
									cmdArgs.add(to + offlinePlayer.getName());
								}
							}
						});	
					}
					if(!argsContainString(args, message)) {
						cmdArgs.add(message);
					}
					if(typeArg != null && !typeArg.isEmpty()) {
						if(!argsContainString(args, typeArg + ":")) {
							cmdArgs.add(typeArg + ":");
						}
					}
					return Utils.getAllStartsWith(args[args.length-1], cmdArgs);
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
