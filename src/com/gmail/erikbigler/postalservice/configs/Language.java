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

		CONSOLE_ERROR("&cCannot run this command from the console!"),
		NO_PERMISSION("&cYou don't have permission to do that!"),
		PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!");

		private String text;

		private Phrases(String text) {
			this.text = text;
		}

		public String toString() {
			return ChatColor.translateAlternateColorCodes('&', this.text);
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
			phrase.text = getCustomConfig().getString(phrase.name(), phrase.text);
			if (!getCustomConfig().isSet(phrase.name())) {
				getCustomConfig().set(phrase.name(), phrase.text);
			}
		}
	}

	private static void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(PostalService.getPlugin().getDataFolder(), "localizations.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	}

	private static FileConfiguration getCustomConfig()
	{
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	private static void saveCustomConfig()
	{
		if ((customConfig == null) || (customConfigFile == null)) {
			return;
		}
		try
		{
			customConfig.save(customConfigFile);
		} catch (IOException ex)
		{
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}
}
