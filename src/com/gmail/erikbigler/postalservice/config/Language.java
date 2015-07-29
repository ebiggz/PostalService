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

	// TODO: Add color and formats to all applicable phrases
	public static enum Phrases {
		// @formatter:off
		PREFIX("&e[PostalService]"),
		ALERT_MAILBOX_FIND("Marking nearby mailboxes!"),
		ALERT_MAILBOX_REG("Mailbox registered!"),
		ALERT_MAILBOX_REMOVE("Please click a chest to unregister it as a mailbox..."),
		ALERT_MAILBOX_REMOVE_ALL("Unregistered all of your mailboxes."),
		ALERT_MAILBOX_REMOVE_ALL_OTHER("Unregistered all mailboxes for %player%."),
		ALERT_MAILBOX_SET("Please click a chest to register it as a mailbox..."),
		ALERT_MAILBOX_TIMEOUT("Mailbox selection timed out."),
		ALERT_MAILBOX_UNREG("Mailbox unregistered!"),
		ALERT_MAILTYPE_EXPERIENCE_CLAIM("You have successfully claimed the experience points."),
		ALERT_MAILTYPE_PACKAGE_CLAIM("You have claimed the contents of this Package."),
		ALERT_MAILTYPE_PAYMENT_CLAIM("You have successfully claimed this Payment."),
		ALERT_NO_UNREAD_MAIL("You don't have any unread mail."),
		ALERT_RECEIVED_MAIL("You just received mail from %sender%! Visit your nearest mailbox to read it."),
		ALERT_SENT_MAIL("You have mailed a %mailtype% to %recipient%."),
		ALERT_UNREAD_MAIL("You have %count% unread mail message(s)!"),
		ALERT_BLACKLISTED_WORLD_OVERRIDE("&eCareful, you are running a command in a world that is normally blacklisted."),
		ALERT_RELOAD_COMPLETE("Reload complete!"),
		ALERT_TIMEZONE_SET("&bTime zone set!"),
		ALERT_UPDATE_DOWNLOAD_BEGUN("&bDownloading %version%..."),
		ALERT_UPDATE_CHECK_BEGUN("&bChecking for updates..."),
		ALERT_UPDATE_DOWNLOAD_SUCCESS("&aAn update was successfully downloaded and it will be available after the next server restart."),
		ERORR_UPDATE_DOWNLOAD_FAIL("&cAn error occured with the updater. Check the console for more details."),
		ERORR_UPDATE_COMMAND_NONE("&aYou are up-to-date!"),
		ALERT_UPDATE_AVAILABLE("An update is available for download!"),
		UPDATE_BUTTON_VIEW_NOTES("[View Update Notes]"),
		UPDATE_BUTTON_VIEW_NOTES_HOVER("Click to view the update notes on Bukkit."),
		UPDATE_BUTTON_DOWNLOAD("[Download]"),
		UPDATE_BUTTON_DOWNLOAD_HOVER("Click to download the update now."),
		BUTTON_ACCOUNTINFO("Account Info"),
		BUTTON_COMPOSE("Compose"),
		BUTTON_COMPOSE_PACKAGE("&e&lCompose Package"),
		BUTTON_INBOX("Inbox"),
		BUTTON_MAINMENU("Main Menu"),
		BUTTON_NEXT("Next ->"),
		BUTTON_PREVIOUS("<- Previous"),
		BUTTON_SENT("Sent"),
		BUTTON_TRADINGPOST("Trading Post"),
		CLAIMED("Claimed"),
		CLICK_ACTION_COMPOSE("&cLeft-Click to &lCompose"),
		CLICK_ACTION_DELETE("&cShift+Right-Click to &lDelete"),
		CLICK_ACTION_DROPBOX("&4Right-Click to &lOpen Drop Box"),
		CLICK_ACTION_HELP("&cLeft-Click for &Help"),
		CLICK_ACTION_LEFTCLAIM("&4Left-Click to &lClaim"),
		CLICK_ACTION_LEFTRETURN("&cLeft-Click to &lReturn"),
		CLICK_ACTION_NEXTPAGE("&cClick for &lNext Page"),
		CLICK_ACTION_OPEN("&cLeft-Click to &lOpen"),
		CLICK_ACTION_PREVIOUSPAGE("&cClick for &lPrevious Page"),
		CLICK_ACTION_RESPOND("&cLeft-Click to &lRespond"),
		CLICK_ACTION_RIGHTCLAIM("&4Right-Click to &lClaim"),
		CLICK_ACTION_RIGHTRETURN("&cRight-Click to &lReturn"),
		COMMAND_ARG_AMOUNT("amount"),
		COMMAND_ARG_CHECK("check"),
		COMMAND_ARG_COMPOSE("compose"),
		COMMAND_ARG_FIND("find"),
		COMMAND_ARG_HELP("help"),
		COMMAND_ARG_MESSAGE("message"),
		COMMAND_ARG_REMOVE("remove"),
		COMMAND_ARG_REMOVEALL("removeall"),
		COMMAND_ARG_TIMEZONE("timezone"),
		COMMAND_ARG_RELOAD("reload"),
		COMMAND_ARG_UPDATE("update"),
		COMMAND_ARG_DOWNLOAD("download"),
		COMMAND_ARG_SET("set"),
		COMMAND_ARG_TO("to"),
		COMMAND_MAIL("mail"),
		COMMAND_MAILBOX("mailbox"),
		COMPOSE_TEXT("&eCompose mail (Click one)"),
		DROPBOX_DESCRIPTION("&7(Drop box is used for sending packages)"),
		DROPBOX_HELP("&e&nDrop Box Help"),
		DROPBOX_HELP_TEXT("You can place items from your inventory anywhere above the dotted line. Once you have, click the Compose Package button to the right. All items in your drop box are sent when you mail the package!"),
		DROPBOX_TITLE("Drop Box"),
		ERROR_CANT_MAIL_YOURSELF("&cYou can't mail yourself. Sorry!"),
		ERROR_CONSOLE_COMMAND("You can't run that command from the console!"),
		ERROR_UNKNOWN_COMMAND("&cUnknown command. Type %command% for help."),
		ERROR_EMPTY_MESSAGE("&cYour message is empty! Please try again."),
		ERROR_INBOX("&cSomething went wrong! Please try opening mail again. Sorry!"),
		ERROR_INVALID_NUMBER("&c%That is not a valid number!"),
		ERROR_MAILBOX_ALREADY_EXISTS("A mailbox already exists here! Selection canceled."),
		ERROR_MAILBOX_BREAK("This chest is registered as a mailbox. The owner, %owner%, must unregister the chest before it can be removed."),
		ERROR_MAILBOX_DOESNT_EXIST("A mailbox doesn't exist here! Selection canceled."),
		ERROR_MAILBOX_FIND_NONE("There are no nearby mailboxes!"),
		ERROR_MAILBOX_NOT_CHEST("Selected block is not a chest! Selection canceled."),
		ERROR_MAILBOX_NOT_OWNER("You do not own this mailbox! Selection canceled."),
		ERROR_MAILBOX_NO_PERM("You do not have permission to register a mailbox here! Selection canceled."),
		ERROR_MAILBOX_UNKNOWN("An unknown error occurred. Please try again."),
		ERROR_MAILTYPE_EXPERIENCE_EMPTY("You must include an xp amount!"),
		ERROR_MAILTYPE_EXPERIENCE_NOTENOUGH("You don't have that amount of XP to send!"),
		ERROR_MAILTYPE_EXPERIENCE_NOTVALID("That is not a recognized amount of XP!"),
		ERROR_MAILTYPE_NOT_FOUND("&c%mailtype% is not a recognized mail type!"),
		ERROR_MAILTYPE_PACKAGE_NEED_SPACE("There is not enough empty room in your inventory. Please clear space and try again."),
		ERROR_MAILTYPE_PACKAGE_NO_ITEMS("You must have items in your drop box to send a Package!"),
		ERROR_MAILTYPE_PAYMENT_EMPTY("You must include an xp amount!"),
		ERROR_MAILTYPE_PAYMENT_NOTENOUGH("You can't send zero or negative money!"),
		ERROR_MAILTYPE_PAYMENT_NOTVALID("You do not have enough in your bank to send that amount of money!"),
		ERROR_NEAR_MAILBOX("&cYou must be near a mailbox to send mail!"),
		ERROR_NO_PERMISSION("&cYou don't have permission to do that!"),
		ERROR_PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!"),
		ERROR_BLACKLISTED_WORLD("&cYou do not have permission to do that in this world!"),
		MAIL_ICON_FROM("from"),
		MAILTYPE_EXPERIENCE("Experience"),
		MAILTYPE_EXPERIENCE_HOVERTEXT("Mail XP points (not levels)!"),
		MAILTYPE_EXPERIENCE_ITEMDESC("%count% XP point(s)"),
		MAILTYPE_LETTER("Letter"),
		MAILTYPE_LETTER_HOVERTEXT("Mail a text-only letter!"),
		MAILTYPE_PACKAGE("Package"),
		MAILTYPE_PACKAGE_CLAIM_BUTTON("Claim Package"),
		MAILTYPE_PACKAGE_HOVERTEXT("Mail in-game items!"),
		MAILTYPE_PACKAGE_ITEMDESC("%count% Required Slot(s)"),
		MAILTYPE_PACKAGE_SUMMARYSCREEN_TITLE("Package Contents"),
		MAILTYPE_PAYMENT("Payment"),
		MAILTYPE_PAYMENT_HOVERTEXT("Mail in-game money!"),
		MAILTYPE_PAYMENT_ITEMDESC("$%count%"),
		MAINMENU_TITLE("Postal Service"),
		REPLY_SUMMARY_MESSAGE("%sender% mailed you a %mailtype% with the message: \"%message%\" at %timestamp%"),
		REPLY_SUMMARY_NOMESSAGE("%sender% mailed you a %mailtype% at %timestamp%"),
		REPLY_TEXT("&eReply with a (Click one)"),
		MAIL_ICON_TO("to"),
		MAIL_ICON_CLAIMWORLDGROUP("Claim in the %worldgroup%");
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
		sb.append(":(\\s)?(\\w+)\\b|$");
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
