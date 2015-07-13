package com.gmail.erikbigler.postalservice.configs;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;

public class ConfigManager {

	public boolean USE_DATABASE = true;
	public List<String> disabledMailTypes;

	protected ConfigManager() { /*exists to block instantiation*/ }
	private static ConfigManager instance = null;
	public static ConfigManager getInstance() {
		if(instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	public void loadAllFiles() {
		loadConfig();
	}

	private void loadConfig() {
		PluginManager pm = PostalService.getPlugin().getServer().getPluginManager();
		String pluginFolder = PostalService.getPlugin().getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		File configFile = new File(pluginFolder, "config.yml");
		if(!configFile.exists()) {
			PostalService.getPlugin().saveResource("config.yml", true);
		}
		try {
			PostalService.getPlugin().reloadConfig();
		} catch (Exception e) {
			PostalService.getPlugin().getLogger().log(Level.SEVERE, "Exception while loading PostalService/config.yml", e);
			pm.disablePlugin(PostalService.getPlugin());
			return;
		}

	}
}
