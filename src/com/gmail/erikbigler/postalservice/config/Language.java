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
		ERROR_CONSOLE_COMMAND("&cCannot run this command from the console!"),
		ERROR_NO_PERMISSION("&cYou don't have permission to do that!"),
		ERROR_PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!"),
		ERROR_INBOX("&cSomething went wrong! Please try opening mail again. Sorry!"),
		ERROR_CANT_MAIL_YOURSELF("&cYou can't mail yourself. Sorry!"),
		ERROR_EMPTY_MESSAGE("&cYour message is empty! Please try again."),
		ERROR_NEAR_MAILBOX("&cYou must be near a mailbox to send mail!"),
		ERROR_MAILTYPE_NOT_FOUND("&c%mailtype% is not a recognized mail type!"),
		ERROR_INVALID_NUMBER("&c%That is not a valid number!"),
		COMMAND_MAIL("mail"),
		COMMAND_MAILBOX("mailbox"),
		COMMAND_ARG_TO("to"),
		COMMAND_ARG_MESSAGE("message"),
		COMMAND_ARG_AMOUNT("amount"),
		COMMAND_ARG_COMPOSE("compose"),
		COMMAND_ARG_CHECK("check"),
		COMMAND_ARG_HELP("help"),
		MAILTYPE_LETTER("Letter"),
		MAILTYPE_EXPERIENCE("Experience"),
		MAILTYPE_PAYMENT("Payment"),
		MAILTYPE_PACKAGE("Package"),
		MAILTYPE_PACKAGE_HOVERTEXT("Mail in-game items!"),
		ALERT_RECEIVED_MAIL("&bYou just received mail from %sender%! Visit your nearest mailbox to read it."),
		ALERT_SENT_MAIL("&bYou have mailed a %mailtype% to %recipient%."),
		ALERT_NO_UNREAD_MAIL("You don't have any unread mail."),
		ALERT_UNREAD_MAIL("You have %count% unread mail message(s)!"),
		CLICK_ACTION_COMPOSE("&cLeft-Click to &lCompose"),
		CLICK_ACTION_DROPBOX("&4Right-Click to &lOpen Drop Box"),
		CLICK_ACTION_OPEN("&cLeft-Click to &lOpen"),
		CLICK_ACTION_HELP("&cLeft-Click for &Help"),
		CLICK_ACTION_LEFTRETURN("&cLeft-Click to &lReturn"),
		CLICK_ACTION_RIGHTRETURN("&cRight-Click to &lReturn"),
		CLICK_ACTION_NEXTPAGE("&cClick for &lNext Page"),
		CLICK_ACTION_PREVIOUSPAGE("&cClick for &lPrevious Page"),
		CLICK_ACTION_RIGHTCLAIM("&4Right-Click to &lClaim"),
		CLICK_ACTION_LEFTCLAIM("&4Left-Click to &lClaim"),
		CLICK_ACTION_DELETE("&cShift+Right-Click to &lDelete"),
		CLICK_ACTION_RESPOND("&cLeft-Click to &lRespond"),
		MAINMENU_TITLE("Postal Service"),
		DROPBOX_DESCRIPTION("&7(The drop box is used for sending packages)"),
		CLAIMED("Claimed"),
		TO("to"),
		FROM("from"),
		BUTTON_PREVIOUS("<- Previous"),
		BUTTON_NEXT("Next ->"),
		BUTTON_MAINMENU("Main Menu"),
		BUTTON_INBOX("Inbox"),
		BUTTON_SENT("Sent"),
		BUTTON_COMPOSE("Compose"),
		BUTTON_TRADINGPOST("Trading Post"),
		BUTTON_ACCOUNTINFO("Account Info"),
		REPLY_TEXT("&eReply with a (Click one)"),
		COMPOSE_TEXT("&eCompose mail (Click one)"),
		REPLY_SUMMARY_NOMESSAGE("%sender% mailed you a %mailtype% at %timestamp%"),
		REPLY_SUMMARY_MESSAGE("%sender% mailed you a %mailtype% with the message \"%message%\" at %timestamp%"),
		DROPBOX_HELP_TEXT("You can place items from your inventory anywhere above the dotted line. Once you have, click the Compose Package button to the right. All items in your drop box are sent when you mail the package!"),
		DROPBOX_HELP("&e&nDrop Box Help"),
		DROPBOX_TITLE("Drop Box"),
		BUTTON_COMPOSE_PACKAGE("&e&lCompose Package");
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
	}

	private static void writeDefaults() {
		Phrases[] arrayOfPhrases = Phrases.values();
		for (Phrases phrase : arrayOfPhrases) {
			if (!getCustomConfig().isSet(phrase.name())) {
				getCustomConfig().set(phrase.name(), phrase.text);
			}
		}
		saveCustomConfig();
	}

	private static void loadValues() {
		Phrases[] arrayOfPhrases = Phrases.values();
		for (Phrases phrase : arrayOfPhrases) {
			phrase.text = ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString(phrase.name(), phrase.text));
		}
	}

	private static void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(PostalService.getPlugin().getDataFolder(), "localizations.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	}

	private static FileConfiguration getCustomConfig() {
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	private static void saveCustomConfig() {
		if ((customConfig == null) || (customConfigFile == null)) {
			return;
		}
		try {
			customConfig.save(customConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}
}
