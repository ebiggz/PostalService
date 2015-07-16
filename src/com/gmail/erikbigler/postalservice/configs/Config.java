package com.gmail.erikbigler.postalservice.configs;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;

public class Config {

	public static void loadFile() {
		loadConfig();
		loadOptions();
	}

	private static void loadConfig() {
		PostalService plugin = PostalService.getPlugin();
		PluginManager pm = plugin.getServer().getPluginManager();
		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		File configFile = new File(pluginFolder, "config.yml");
		if (!configFile.exists()) {
			PostalService.getPlugin().saveResource("config.yml", true);
		}
		try {
			PostalService.getPlugin().reloadConfig();
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "Exception while loading PostalService/config.yml", e);
			pm.disablePlugin(plugin);
		}
	}

	private static void loadOptions() {

	}

}
