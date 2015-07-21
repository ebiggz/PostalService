package com.gmail.erikbigler.postalservice.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.FormattedText;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessage;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.ClickEvent;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.HoverEvent;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.screens.MainMenuGUI;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class MailCommands implements CommandExecutor {

	enum Tracking {
		TO, MESSAGE, ATTACHMENT
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player cmdPlayer = (Player) sender;
		if (commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAIL.toString()) || commandLabel.equalsIgnoreCase("mail")) {
			if(commandLabel.equalsIgnoreCase("mailadmin") && !sender.isOp()){
				return true;
			}

			if(args.length == 0) {
				GUIManager.getInstance().showGUI(new MainMenuGUI(), cmdPlayer);
				//FancyMenu.showClickableCommandList(sender, commandLabel, "Mythian Postal Service", commandData, 1);
			}
			else if(args.length == 1) {
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_COMPOSE.toString())) {
					if(Config.REQUIRE_NEARBY_MAILBOX) {
						boolean nearMailbox = MailboxManager.getInstance().mailboxIsNearby(cmdPlayer.getLocation(), 6);
						if(!nearMailbox) {
							sender.sendMessage(Phrases.ERROR_NEAR_MAILBOX.toPrefixedString());
							return true;
						}
					}
					InteractiveMessage im = new InteractiveMessage();
					im.addElement(Phrases.COMPOSE_TEXT.toPrefixedString() + ": ");
					MailType[] types = MailManager.getInstance().getMailTypes();
					int remaining = types.length;
					for(MailType type : types) {
						String attachArg = "";
						if(type.getAttachmentCommandArgument() != null && !type.getAttachmentCommandArgument().isEmpty()) {
							attachArg = " " + type.getAttachmentCommandArgument() + ":";
						}
						InteractiveMessageElement ime = new InteractiveMessageElement(
								new FormattedText(ChatColor.stripColor(type.getDisplayName()), ChatColor.AQUA),
								HoverEvent.SHOW_TEXT,
								new FormattedText(ChatColor.stripColor(type.getHoveroverDescription()), ChatColor.GOLD),
								ClickEvent.SUGGEST_COMMAND,
								"/" + Phrases.COMMAND_MAIL.toString() + " " + type.getDisplayName().toLowerCase() + " " + Phrases.COMMAND_ARG_TO.toString() + ": " + Phrases.COMMAND_ARG_MESSAGE.toString() + ":" + attachArg);
						im.addElement(ime);
						remaining--;
						if(remaining > 0) {
							im.addElement(", ", ChatColor.AQUA);
						}
					}
					im.sendTo(sender);
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_HELP.toString())) {
					//FancyMenu.showClickableCommandList(sender, commandLabel, "Mythian Postal Service Help", helpData, 1);
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_CHECK.toString())) {
					User user = UserFactory.getUser(sender.getName());
					Utils.unreadMailAlert(user, false);
				}
			}

			else {
				//check if a mailbox should be near by
				if(Config.REQUIRE_NEARBY_MAILBOX) {
					boolean nearMailbox = MailboxManager.getInstance().mailboxIsNearby(cmdPlayer.getLocation(), 6);
					if(!nearMailbox) {
						sender.sendMessage(Phrases.ERROR_NEAR_MAILBOX.toPrefixedString());
						return true;
					}
				}

				MailType mailType = MailManager.getInstance().getMailTypeByName(args[0]);

				if(mailType == null) {
					sender.sendMessage(Phrases.ERROR_MAILTYPE_NOT_FOUND.toPrefixedString().replace("%mailtype%", args[0]));
					return true;
				}
				String to = "", message = "", attachmentArgs = "";
				Tracking tracking = null;
				for(int i = 1; i < args.length; i++) {
					if(args[i].startsWith(Phrases.COMMAND_ARG_TO.toString().toLowerCase() +":")) {
						tracking = Tracking.TO;
						to += args[i].replace(Phrases.COMMAND_ARG_TO.toString().toLowerCase() +":", "");
					}
					else if(args[i].startsWith(Phrases.COMMAND_ARG_MESSAGE.toString().toLowerCase() +":")) {
						tracking = Tracking.MESSAGE;
						message += args[i].replace(Phrases.COMMAND_ARG_MESSAGE.toString().toLowerCase() +":", "");
					}
					else if(args[i].startsWith(mailType.getAttachmentCommandArgument() + ":")) {
						tracking = Tracking.ATTACHMENT;
						attachmentArgs += args[i].replace(mailType.getAttachmentCommandArgument()+ ":", "");
					}
					else {
						if(tracking == null) continue;
						switch(tracking) {
						case TO:
							to += " " + args[i];
							break;
						case MESSAGE:
							message += " "+ args[i];
							break;
						case ATTACHMENT:
							attachmentArgs += " " + args[i];
							break;
						default:
							break;
						}
					}
				}
				to = to.trim();
				message = message.trim();
				attachmentArgs = attachmentArgs.trim();
				if(to.isEmpty()) {
					sender.sendMessage(Phrases.ERROR_PLAYER_NOT_FOUND.toPrefixedString());
					return true;
				}
				String completedName = Utils.completeName(to);
				if(completedName == null) {
					completedName = to;
				}
				if(completedName.equals(sender.getName())) {
					sender.sendMessage(Phrases.ERROR_CANT_MAIL_YOURSELF.toPrefixedString());
					return true;
				}

				if(message.isEmpty()) {
					if(cmdPlayer.getItemInHand().getType() == Material.BOOK_AND_QUILL) {
						BookMeta bm = (BookMeta) cmdPlayer.getItemInHand().getItemMeta();
						if(bm.hasPages()) {
							if(bm.getPageCount() > 1) {
								sender.sendMessage(ChatColor.RED + "[MPS] Books can only contain 1 page of text to use them to send a message!");
								return true;
							}
							message = bm.getPage(1).trim();
						}
					} else {
						if(mailType.requireMessage()) {
							sender.sendMessage(Phrases.ERROR_EMPTY_MESSAGE.toPrefixedString());
							return true;
						}
					}
				}

				try {
					attachmentArgs = attachmentArgs.trim();
					String[] attachmentData;
					if(attachmentArgs.length() < 1){
						attachmentData = new String[0];
					} else {
						attachmentData = attachmentArgs.split(" ");
					}
					User user = UserFactory.getUser(sender.getName());
					user.sendMail(completedName, message, mailType.handleSendCommand(cmdPlayer, attachmentData), mailType, Config.getCurrentWorldGroupForUser(user));
					sender.sendMessage(Phrases.ALERT_SENT_MAIL.toPrefixedString().replace("%mailtype%", mailType.getDisplayName()).replace("%recipient%", completedName));
				} catch (MailException e) {
					sender.sendMessage(Phrases.PREFIX.toString() + " " + e.getErrorMessage());
					return true;
				}
			}
		}
		return true;
	}
}
