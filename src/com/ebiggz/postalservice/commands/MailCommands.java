package com.ebiggz.postalservice.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.apis.guiAPI.GUIManager;
import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.events.PlayerOpenMailMenuEvent;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.MailManager;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.mailbox.MailboxManager;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.screens.MainMenuGUI;
import com.ebiggz.postalservice.utils.UUIDUtils;
import com.ebiggz.postalservice.utils.Updater.UpdateResult;
import com.ebiggz.postalservice.utils.Utils;

public class MailCommands implements CommandExecutor {

	enum Tracking {
		TO, MESSAGE, ATTACHMENT
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		boolean isConsole = false;

		Player player = null;

		if(sender instanceof Player) {
			player = (Player) sender;
			if(Config.playerIsInBlacklistedWorld(player)) {
				if(!PermissionHandler.playerHasPermission(Perm.OVERRIDE_WORLD_BLACKLIST, sender, false)) {
					sender.sendMessage(Phrases.ERROR_BLACKLISTED_WORLD.toPrefixedString());
					return true;
				} else {
					sender.sendMessage(Phrases.ALERT_BLACKLISTED_WORLD_OVERRIDE.toPrefixedString());
				}
			}
		} else {
			isConsole = true;
		}
		if (commandLabel.equalsIgnoreCase(Phrases.COMMAND_MAIL.toString()) || commandLabel.equalsIgnoreCase("mail") || commandLabel.equalsIgnoreCase("postalservice") || commandLabel.equalsIgnoreCase("ps") || commandLabel.equalsIgnoreCase("m")) {
			if(args.length == 0) {
				if(senderIsConsole(sender, isConsole)) return true;
				if(!PermissionHandler.playerHasPermission(Perm.MAIL_READ, sender, true)) return true;
				if(Config.REQUIRE_MAILBOX && !PermissionHandler.playerHasPermission(Perm.OVERRIDE_REQUIRE_MAILBOX, sender, false)) {
					Utils.fancyHelpMenu(sender, commandLabel + " " + Phrases.COMMAND_ARG_HELP.toString()).sendPage(1, sender);
				} else {
					User user = UserFactory.getUser(player);
					Bukkit.getServer().getPluginManager().callEvent(new PlayerOpenMailMenuEvent(player, user, null));
					GUIManager.getInstance().showGUI(new MainMenuGUI(user, false), player);
				}
				return true;
			}
			else if(args.length == 1) {
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_COMPOSE.toString())) {
					if(senderIsConsole(sender, isConsole)) return true;
					//check if a mailbox should be near by
					if(Config.REQUIRE_MAILBOX && !PermissionHandler.playerHasPermission(Perm.OVERRIDE_REQUIRE_MAILBOX, sender, false)) {
						boolean nearMailbox = MailboxManager.getInstance().mailboxIsNearby(player.getLocation());
						if(!nearMailbox) {
							sender.sendMessage(Phrases.ERROR_NEAR_MAILBOX.toPrefixedString());
							return true;
						}
					}
					Utils.getComposeMessage(false, player).sendTo(sender);
					return true;
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_HELP.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.HELP, sender, true)) return true;
					Utils.fancyHelpMenu(sender, commandLabel + " " + Phrases.COMMAND_ARG_HELP.toString()).sendPage(1, sender);
					return true;
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_CHECK.toString())) {
					if(senderIsConsole(sender, isConsole)) return true;
					if(!PermissionHandler.playerHasPermission(Perm.MAIL_CHECK, sender, true)) return true;
					User user = UserFactory.getUser(sender.getName());
					Utils.unreadMailAlert(user, false);
					return true;
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_RELOAD.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.RELOAD, sender, true)) return true;
					Config.loadFile();
					Language.loadFile();
					UUIDUtils.loadFile();
					MailManager.getInstance().loadEnabledMailTypes();
					sender.sendMessage(Phrases.ALERT_RELOAD_COMPLETE.toPrefixedString());
					return true;
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_DOWNLOAD.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.UPDATE, sender, true)) return true;
					if(PostalService.getUpdater().getResult() == UpdateResult.UPDATE_AVAILABLE) {
						sender.sendMessage(Phrases.ALERT_UPDATE_DOWNLOAD_BEGUN.toPrefixedString().replace("%version%", PostalService.getUpdater().getLatestName().replace("PostalService v", "")));
						PostalService.downloadUpdate(sender);
					} else {
						sender.sendMessage(Phrases.ERORR_UPDATE_COMMAND_NONE.toPrefixedString());
					}
					return true;
				}
				else if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_UPDATE.toString())) {
					if(!PermissionHandler.playerHasPermission(Perm.UPDATE, sender, true)) return true;
					sender.sendMessage(Phrases.ALERT_UPDATE_CHECK_BEGUN.toPrefixedString());
					PostalService.manualUpdateCheck(sender);
					return true;
				}
				else if(PermissionHandler.playerHasPermission(Perm.MAIL_READOTHER, sender, false)){
					if(senderIsConsole(sender, isConsole)) return true;
					String completedName = Utils.completeName(args[0]);
					if(completedName == null || completedName.isEmpty()) {
						if(Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore()) {
							GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(args[0]), false), player);
							return true;
						}
					} else {
						GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(completedName), false), player);
						return true;
					}
				}
			}

			else {
				if(senderIsConsole(sender, isConsole)) return true;
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_HELP.toString())) {
					try {
						int pageNumber = Integer.parseInt(args[1]);
						Utils.fancyHelpMenu(sender, commandLabel + " " + Phrases.COMMAND_ARG_HELP.toString()).sendPage(pageNumber, sender);
					} catch (Exception e) {
						sender.sendMessage(Phrases.ERROR_INVALID_NUMBER.toPrefixedString());
					}
					return true;
				}
				if(args[0].equalsIgnoreCase(Phrases.COMMAND_ARG_TIMEZONE.toString())) {
					UserFactory.getUser(player).setTimeZone(args[1].toUpperCase());
					sender.sendMessage(Phrases.ALERT_TIMEZONE_SET.toPrefixedString());
					return true;
				}

				//check if a mailbox should be near by
				if(Config.REQUIRE_MAILBOX && !PermissionHandler.playerHasPermission(Perm.OVERRIDE_REQUIRE_MAILBOX, sender, false)) {
					boolean nearMailbox = MailboxManager.getInstance().mailboxIsNearby(player.getLocation());
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
				if(!PermissionHandler.playerCanMailType(mailType.getDisplayName(), sender)) {
					sender.sendMessage(Phrases.ERROR_NO_PERMISSION.toPrefixedString());
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
					if(!Bukkit.getOfflinePlayer(completedName).hasPlayedBefore()) {
						sender.sendMessage(Phrases.ERROR_PLAYER_NOT_FOUND.toPrefixedString());
						return true;
					}
				}

				if(completedName.equals(sender.getName()) && !PermissionHandler.playerHasPermission(Perm.MAIL_SELF, sender, false)) {
					sender.sendMessage(Phrases.ERROR_CANT_MAIL_YOURSELF.toPrefixedString());
					return true;
				}

				if(message.isEmpty()) {
					if(player.getInventory().getItemInMainHand().getType() == Material.WRITABLE_BOOK) {
						BookMeta bm = (BookMeta) player.getItemInHand().getItemMeta();
						if(bm.hasPages()) {
							if(bm.getPageCount() > 1) {
								sender.sendMessage(Phrases.ERROR_BOOK_TOOLONG.toPrefixedString());
								return true;
							}
							if(bm.getPage(1).trim().isEmpty()) {
								sender.sendMessage(Phrases.ERROR_EMPTY_MESSAGE.toPrefixedString());
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
					user.sendMail(completedName, message, mailType.handleSendCommand(player, attachmentData), mailType, Config.getCurrentWorldGroupForUser(user));
				} catch (MailException e) {
					sender.sendMessage(Phrases.PREFIX.toString() + " " + e.getErrorMessage());
				}
				return true;
			}
		}
		sender.sendMessage(Phrases.ERROR_UNKNOWN_COMMAND.toPrefixedString().replace("%command%", "/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_HELP.toString()));
		return true;
	}

	private boolean senderIsConsole(CommandSender sender, boolean isConsole) {
		if(isConsole) {
			sender.sendMessage(Phrases.ERROR_CONSOLE_COMMAND.toPrefixedString());
			return true;
		}
		return false;
	}
}
