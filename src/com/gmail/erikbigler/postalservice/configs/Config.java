package com.gmail.erikbigler.postalservice.configs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.worldgroups.WorldGroup;

public class Config {

	// General Settings
	public static boolean ENABLE_WORLD_GROUPS;
	public static List<WorldGroup> WORLD_GROUPS;
	public static List<String> MAILTYPES_IGNORE_WORLD_GROUPS;
	public static List<String> WORLD_BLACKLIST;
	public static List<String> ENABLED_MAILTYPES;
	public static boolean ENABLE_DEBUG;
	public static boolean USE_DATABASE = true;
	// User Settings
	public static HashMap<String, Integer> INBOX_SIZES;
	public static boolean UNREAD_NOTIFICATION_WORLD_CHANGE;
	public static boolean UNREAD_NOTIFICATION_LOGIN;
	public static boolean USE_UUIDS;
	// Mailbox Settings
	public static boolean ENABLE_MAILBOXES;
	public static boolean REQUIRE_NEARBY_MAILBOX;
	public static HashMap<String, Integer> MAILBOX_LIMITS;
	// Trading Post Settings
	public static boolean ENABLE_TRADINGPOST;
	public static boolean REQUIRE_SAME_MAILBOX;
	public static boolean REQUIRE_CROSS_WORLD_TRADES;

	private static double CONFIG_VERSION = 1.0;

	public static void loadFile() {
		loadConfig();
		loadOptions();
		if (CONFIG_VERSION != 1.0) {
			// out of date config version
		}
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
		FileConfiguration config = PostalService.getPlugin().getConfig();
		/** Load world group options */
		ENABLE_WORLD_GROUPS = config.getBoolean("enable-world-groups", false);
		WORLD_GROUPS = new ArrayList<WorldGroup>();
		ConfigurationSection wgConfigSec = config.getConfigurationSection("world-groups");
		if (wgConfigSec != null) {
			for (String worldGroupName : wgConfigSec.getKeys(false)) {
				List<String> worldNames = wgConfigSec.getStringList(worldGroupName);
				if (worldNames.isEmpty()) {
					// log error, empty world group
					continue;
				}
				WORLD_GROUPS.add(new WorldGroup(worldGroupName, worldNames));
			}
		}
		if (ENABLE_WORLD_GROUPS && WORLD_GROUPS.isEmpty()) {
			// log error, no world groups but feature is enabled.
			ENABLE_WORLD_GROUPS = false;
		}

	}

	public static boolean mailTypeIsEnabled(MailType mailType) {
		for (String mailTypeName : ENABLED_MAILTYPES) {
			if (mailType.getName().equalsIgnoreCase(mailTypeName))
				return true;
		}
		return false;
	}

	public static WorldGroup getWorldGroupFromWorld(String worldName) {
		for (WorldGroup worldGroup : WORLD_GROUPS) {
			if (worldGroup.hasWorld(worldName))
				return worldGroup;
		}
		return null;
	}
}
