package com.gmail.erikbigler.postalservice.configs;

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
		PREFIX("[PostalService]"), 
		ERROR_CONSOLE_COMMAND("&cCannot run this command from the console!"), 
		ERROR_NO_PERMISSION("&cYou don't have permission to do that!"), 
		ERROR_PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!"),
		COMMAND_MAIL("mail"),
		COMMAND_ARG_TO("to"),
		COMMAND_ARG_MESSAGE("message"),
		COMMAND_ARG_AMOUNT("amount"),
		MAILTYPE_LETTER("Letter"),
		MAILTYPE_EXPERIENCE("Experience"),
		MAILTYPE_PAYMENT("Payment"),
		MAILTYPE_PACKAGE("Package");
		// @formatter:on
		private String text;

		private Phrases(String text) {
			this.text = text;
		}

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
			phrase.text = getCustomConfig().getString(phrase.name(), ChatColor.translateAlternateColorCodes('&', phrase.text));
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
