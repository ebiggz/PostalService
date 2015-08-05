package com.gmail.erikbigler.postalservice.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.erikbigler.postalservice.PostalService;

public class Language {

	private static FileConfiguration customConfig = null;
	private static File customConfigFile = null;

	public static void loadFile() {
		reloadCustomConfig();
		writeDefaults();
		loadValues();
	}

	public static enum Phrases {
		// @formatter:off
		PREFIX("&e[PostalService]"),
		ACCOUNT_INFO_INBOXSIZE("Inbox Size"),
		ACCOUNT_INFO_MAILBOXES("Mailboxes"),
		ALERT_BLACKLISTED_WORLD_OVERRIDE("&cCareful, you are running a command in a world that is normally blacklisted."),
		ALERT_MAILBOX_FIND("&bMarking nearby mailboxes!"),
		ALERT_MAILBOX_REG("&bMailbox registered!"),
		ALERT_MAILBOX_REMOVE("&bPlease click a chest to unregister it as a mailbox..."),
		ALERT_MAILBOX_REMOVE_ALL("&bUnregistered all of your mailboxes.’"),
		ALERT_MAILBOX_REMOVE_ALL_OTHER("&bUnregistered all mailboxes for %player%."),
		ALERT_MAILBOX_SET("&bPlease click a chest to register it as a mailbox..."),
		ALERT_MAILBOX_TIMEOUT("&bMailbox selection timed out."),
		ALERT_MAILBOX_UNREG("&bMailbox unregistered!"),
		ALERT_MAILTYPE_EXPERIENCE_CLAIM("&bYou have successfully claimed the experience points."),
		ALERT_MAILTYPE_PACKAGE_CLAIM("&bYou have claimed the contents of this Package."),
		ALERT_MAILTYPE_PAYMENT_CLAIM("&bYou have successfully claimed this Payment."),
		ALERT_NO_UNREAD_MAIL("&bYou don't have any unread mail."),
		ALERT_RECEIVED_MAIL("&bYou just received mail from &e%sender%&b! Visit your nearest mailbox to read it."),
		ALERT_RELOAD_COMPLETE("Reload complete!"),
		ALERT_SENT_MAIL("&bYou have mailed a &e%mailtype%&b to &e%recipient%."),
		ALERT_TIMEZONE_SET("&bTime zone set!"),
		ALERT_UNREAD_MAIL("&bYou have &e%count% &bunread mail message(s)!"),
		ALERT_UPDATE_AVAILABLE("&bAn update is available for download!"),
		ALERT_UPDATE_CHECK_BEGUN("&bChecking for updates..."),
		ALERT_UPDATE_DOWNLOAD_BEGUN("&bDownloading %version%..."),
		ALERT_UPDATE_DOWNLOAD_SUCCESS("&aAn update was successfully downloaded and it will be available after the next server restart."),
		BUTTON_ACCOUNTINFO("&e&lAccount Info"),
		BUTTON_COMPOSE("&e&lCompose"),
		BUTTON_COMPOSE_PACKAGE("&e&lCompose Package"),
		BUTTON_INBOX("&e&lInbox"),
		BUTTON_INBOX_UNREAD("%count% Unread"),
		BUTTON_MAINMENU("&e&lMain Menu"),
		BUTTON_NEXT("Next &l->"),
		BUTTON_PREVIOUS("&l<-&r Previous"),
		BUTTON_SENT("&e&lSent"),
		BUTTON_TRADINGPOST("&e&lTrading Post"),
		CLAIMED("Claimed"),
		CLICK_ACTION_COMPOSE("&cLeft-Click to &lCompose"),
		CLICK_ACTION_DELETE("&cShift+Right-Click to &lDelete"),
		CLICK_ACTION_DROPBOX("&cRight-Click to &lOpen Drop Box"),
		CLICK_ACTION_HELP("&cLeft-Click for &lHelp"),
		CLICK_ACTION_LEFTCLAIM("&cLeft-Click to &lClaim"),
		CLICK_ACTION_LEFTRETURN("&cLeft-Click to &lReturn"),
		CLICK_ACTION_NEXTPAGE("&cClick for &lNext Page"),
		CLICK_ACTION_OPEN("&cLeft-Click to &lOpen"),
		CLICK_ACTION_PREVIOUSPAGE("&cClick for &lPrevious Page"),
		CLICK_ACTION_RESPOND("&cLeft-Click to &lRespond"),
		CLICK_ACTION_RIGHTCLAIM("&cRight-Click to &lClaim"),
		CLICK_ACTION_RIGHTRETURN("&cRight-Click to &lReturn"),
		COMMAND_ARG_AMOUNT("amount"),
		COMMAND_ARG_CHECK("check"),
		COMMAND_ARG_COMPOSE("compose"),
		COMMAND_ARG_DOWNLOAD("download"),
		COMMAND_ARG_FIND("find"),
		COMMAND_ARG_HELP("help"),
		COMMAND_ARG_MESSAGE("message"),
		COMMAND_ARG_RELOAD("reload"),
		COMMAND_ARG_REMOVE("remove"),
		COMMAND_ARG_REMOVEALL("removeall"),
		COMMAND_ARG_SET("set"),
		COMMAND_ARG_TIMEZONE("timezone"),
		COMMAND_ARG_TO("to"),
		COMMAND_ARG_UPDATE("update"),
		COMMAND_MAIL("mail"),
		COMMAND_MAILBOX("mailbox"),
		COMPOSE_TEXT("&eCompose mail (Click one)"),
		DROPBOX_DESCRIPTION("&7(Drop box is used for sending packages)"),
		DROPBOX_HELP("&e&nDrop Box Help"),
		DROPBOX_HELP_TEXT("You can place items from your inventory anywhere above the dotted line. Once you have, click the Compose Package button to the right. All items in your drop box are sent when you mail the package!"),
		DROPBOX_TITLE("Drop Box"),
		ERORR_UPDATE_COMMAND_NONE("&aYou are up-to-date!"),
		ERORR_UPDATE_DOWNLOAD_FAIL("&cAn error occured with the updater. Check the console for more details."),
		ERROR_BLACKLISTED_WORLD("&cYou do not have permission to do that in this world!"),
		ERROR_BOOK_TOOLONG("&cBooks can only contain 1 page of text to use to them send a message."),
		ERROR_CANT_MAIL_YOURSELF("&cYou can't mail yourself. Sorry!"),
		ERROR_CONSOLE_COMMAND("&cYou can't run that command from the console!"),
		ERROR_EMPTY_MESSAGE("&cYour message is empty! Please try again."),
		ERROR_INBOX("&cSomething went wrong! Please try opening mail again. Sorry!"),
		ERROR_INBOX_FULL("&cMail was not sent because %recipient% has a full inbox."),
		ERROR_INVALID_NUMBER("&c%That is not a valid number!"),
		ERROR_MAILBOX_ALREADY_EXISTS("&cA mailbox already exists here! Selection canceled."),
		ERROR_MAILBOX_BREAK("&cThis chest is registered as a mailbox. The owner, &l%owner%&r&c, must unregister the chest before it can be removed."),
		ERROR_MAILBOX_DOESNT_EXIST("&cA mailbox doesn't exist here! Selection canceled."),
		ERROR_MAILBOX_FIND_NONE("&cThere are no nearby mailboxes!"),
		ERROR_MAILBOX_NOT_CHEST("&cSelected block is not a chest! Selection canceled."),
		ERROR_MAILBOX_NOT_OWNER("&cYou do not own this mailbox! Selection canceled."),
		ERROR_MAILBOX_NO_PERM("&cYou do not have permission to register a mailbox here! Selection canceled."),
		ERROR_MAILBOX_UNKNOWN("&cAn unknown error occurred. Please try again."),
		ERROR_MAILTYPE_EXPERIENCE_EMPTY("&cYou must include an xp amount!"),
		ERROR_MAILTYPE_EXPERIENCE_NOTENOUGH("&cYou don't have that amount of XP to send!"),
		ERROR_MAILTYPE_EXPERIENCE_NOTVALID("&cThat is not a recognized amount of XP!"),
		ERROR_MAILTYPE_NOT_FOUND("&c%mailtype% is not a recognized mail type!"),
		ERROR_MAILTYPE_PACKAGE_NEED_SPACE("&cThere is not enough empty room in your inventory. Please clear space and try again."),
		ERROR_MAILTYPE_PACKAGE_NO_ITEMS("&cYou must have items in your drop box to send a Package!"),
		ERROR_MAILTYPE_PAYMENT_EMPTY("&cYou must include an xp amount!"),
		ERROR_MAILTYPE_PAYMENT_NOTENOUGH("&cYou can't send zero or negative money!"),
		ERROR_MAILTYPE_PAYMENT_NOTVALID("&cYou do not have enough in your bank to send that amount of money!"),
		ERROR_NEAR_MAILBOX("&cYou must be near a mailbox to send mail!"),
		ERROR_NO_PERMISSION("&cYou don't have permission to do that!"),
		ERROR_PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!"),
		ERROR_UNKNOWN_COMMAND("&cUnknown command. Type %command% for help."),
		HELPMENU_CHECK_DESC("Checks if you have any unread mail."),
		HELPMENU_COMPOSE_DESC("Gives you a clickable list of mail you can send."),
		HELPMENU_DOWNLOAD_DESC("Download the latest update, if there is one."),
		HELPMENU_FIND_DESC("Marks nearby mailboxes with a green beacon."),
		HELPMENU_HELP_DESC("This help menu."),
		HELPMENU_MAIL_DESC("Opens the GUI for your inbox."),
		HELPMENU_NEXT_BUTTON("Next"),
		HELPMENU_NEXT_DESC("Click for next page."),
		HELPMENU_PAGE("Page"),
		HELPMENU_PLAYER_VARIABLE("[player]"),
		HELPMENU_PREVIOUS_BUTTON("Previous"),
		HELPMENU_PREVIOUS_DESC("Click for previous page."),
		HELPMENU_READOTHER_DESC("View the inbox of the given player."),
		HELPMENU_RELOAD_DESC("Reload the config files."),
		HELPMENU_REMOVEALLOTHER_DESC("Unregister all mailboxes for the given player."),
		HELPMENU_REMOVEALL_DESC("Unregister all your mailboxes"),
		HELPMENU_REMOVE_DESC("Unregister a chest as a mailbox."),
		HELPMENU_SET_DESC("Register a chest as a mailbox."),
		HELPMENU_TIMEZONE_DESC("Set your timezone for the timestamps shown on mail items."),
		HELPMENU_TIMEZONE_VARIABLE("[timezone]"),
		HELPMENU_TIP("&7&o(Hover over a &a&ocommand&7&o for info, click to run it."),
		HELPMENU_TITLE("PostalService Commands"),
		HELPMENU_UPDATE_DESC("Check for any updates."),
		INBOX_PERCENT_FULL("&7[%percent%% full]"),
		MAILTYPE_EXPERIENCE("XP"),
		MAILTYPE_EXPERIENCE_HOVERTEXT("Mail XP points (not levels)!"),
		MAILTYPE_EXPERIENCE_ITEMDESC("%count% XP point(s)"),
		MAILTYPE_LETTER("Letter"),
		MAILTYPE_LETTER_HOVERTEXT("Mail a text-only letter!"),
		MAILTYPE_PACKAGE("Package"),
		MAILTYPE_PACKAGE_CLAIM_BUTTON("&e&lClaim Package"),
		MAILTYPE_PACKAGE_HOVERTEXT("Mail in-game items!"),
		MAILTYPE_PACKAGE_ITEMDESC("%count% Required Slot(s)"),
		MAILTYPE_PACKAGE_SUMMARYSCREEN_TITLE("Package Contents"),
		MAILTYPE_PAYMENT("Payment"),
		MAILTYPE_PAYMENT_HOVERTEXT("Mail in-game money!"),
		MAILTYPE_PAYMENT_ITEMDESC("$%count%"),
		MAIL_ICON_CLAIMWORLDGROUP("&7(Claimable in the %worldgroup%)"),
		MAIL_ICON_FROM("from"),
		MAIL_ICON_TO("to"),
		MAINMENU_TITLE("Postal Service"),
		REPLY_SUMMARY_MESSAGE("&7%sender% mailed you a %mailtype% with the message \"%message%\" at %timestamp%"),
		REPLY_SUMMARY_NOMESSAGE("&7%sender% mailed you a %mailtype% at %timestamp%"),
		REPLY_TEXT("&eReply with a (Click one)"),
		UPDATE_BUTTON_DOWNLOAD("[Download]"),
		UPDATE_BUTTON_DOWNLOAD_HOVER("Click to download the update now."),
		UPDATE_BUTTON_VIEW_NOTES("[View Update Notes]"),
		UPDATE_BUTTON_VIEW_NOTES_HOVER("Click to view the update notes on Bukkit.");
		// @formatter:on
		private String text;

		private Phrases(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return this.text;
		}

		public String toPrefixedString() {
			return PREFIX + " " + this.text;
		}

		public ChatColor getFirstColor() {
			for (int index = 0; index < text.length(); index++) {
				char section = text.charAt(index);
				if ((section == '§') && (index < text.length() - 1)) {
					char c = text.charAt(index + 1);
					ChatColor color = ChatColor.getByChar(c);

					if (color != null) {
						if ((color.isColor()) || (color.equals(ChatColor.RESET)))
						{
							return color;
						}
					}
				}
			}
			return null;
		}

		public ChatColor getFirstFormat() {
			for (int index = 0; index < text.length(); index++) {
				char section = text.charAt(index);
				if ((section == '§') && (index < text.length() - 1)) {
					char c = text.charAt(index + 1);
					ChatColor color = ChatColor.getByChar(c);

					if (color != null) {
						if ((color.isFormat()) || (color.equals(ChatColor.RESET)))
						{
							break;
						}
					}
				}
			}
			return null;
		}
	}

	public static String getToRegex() {
		String to = Phrases.COMMAND_ARG_TO.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("(\\s?)");
		for(char letter : to.toCharArray()) {
			sb.append("[");
			if(Character.isAlphabetic(letter)) {
				sb.append(Character.toUpperCase(letter));
				sb.append(Character.toLowerCase(letter));
			} else {
				sb.append(letter);
			}
			sb.append("]");
		}
		sb.append(":(\\s?)(\\w+)(\\b?|$?|\\s?)");
		return sb.toString();
	}

	private static void writeDefaults() {
		Phrases[] arrayOfPhrases = Phrases.values();
		for(Phrases phrase : arrayOfPhrases) {
			if(!getCustomConfig().isSet(phrase.name())) {
				getCustomConfig().set(phrase.name(), phrase.text);
			}
		}
		saveCustomConfig();
	}

	private static void loadValues() {
		Phrases[] arrayOfPhrases = Phrases.values();
		for(Phrases phrase : arrayOfPhrases) {
			phrase.text = ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString(phrase.name(), phrase.text));
		}
	}

	private static void reloadCustomConfig() {
		if(customConfigFile == null) {
			customConfigFile = new File(PostalService.getPlugin().getDataFolder(), "localizations.yml");
		}
		if(!customConfigFile.exists()) {
			PostalService.getPlugin().saveResource("localizations.yml", true);
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	}

	private static FileConfiguration getCustomConfig() {
		if(customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	private static void saveCustomConfig() {
		if((customConfig == null) || (customConfigFile == null)) {
			return;
		}
		try {
			customConfig.save(customConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}
}
