package com.gmail.erikbigler.postalservice.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class Config {

	// General Settings
	public static boolean ENABLE_WORLD_GROUPS;
	public static List<WorldGroup> WORLD_GROUPS;
	private static List<String> MAILTYPES_IGNORE_WORLD_GROUPS;
	public static List<String> WORLD_BLACKLIST;
	private static List<String> DISABLED_MAILTYPES;
	public static boolean ENABLE_DEBUG;
	public static boolean USE_DATABASE = true;
	public static boolean USE_UUIDS;
	public static boolean FORCE_UUIDS = false;
	public static boolean CHECK_FOR_UPDATES;
	public static AutoDownloadType AUTO_DOWNLOAD_TYPE;

	// User Settings
	public static Map<String, Integer> INBOX_SIZES;
	public static boolean UNREAD_NOTIFICATION_ON_RECEIVE;
	public static boolean UNREAD_NOTIFICATION_LOGIN;
	public static boolean HARD_ENFORCE_INBOX_LIMIT;

	// Mailbox Settings
	public static boolean ENABLE_MAILBOXES;
	public static boolean REQUIRE_MAILBOX;
	public static Map<String, Integer> MAILBOX_LIMITS;
	// Trading Post Settings
	public static boolean ENABLE_TRADINGPOST;
	public static boolean REQUIRE_SAME_MAILBOX;
	public static boolean ALLOW_CROSS_WORLD_TRADES;
	// Language Settings
	public static String DATE_FORMAT;
	public static String LOCALE_TAG;
	public static String DEFAULT_TIMEZONE;
	public static List<String> TIMEZONES;

	private static double CONFIG_VERSION = 1.0;

	public static enum AutoDownloadType {
		BUGFIXES, ALL, NONE
	}

	public static void loadFile() {
		loadConfig();
		loadOptions();
		if(CONFIG_VERSION != 1.0) {
			PostalService.getPlugin().getLogger().warning("Your config file appears to be out of date and there may be new options. Rename your current config to have a new one generate!");
		}
	}

	private static void loadConfig() {
		Plugin plugin = PostalService.getPlugin();
		PluginManager pm = plugin.getServer().getPluginManager();
		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		File configFile = new File(pluginFolder, "config.yml");
		if(!configFile.exists()) {
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
		CONFIG_VERSION = config.getDouble("config-version");
		ENABLE_DEBUG = config.getBoolean("debug-mode", false);
		/* Load world group options */
		ENABLE_WORLD_GROUPS = config.getBoolean("enable-world-groups", false);
		WORLD_GROUPS = new ArrayList<WorldGroup>();
		ConfigurationSection wgConfigSec = config.getConfigurationSection("world-groups");
		if(wgConfigSec != null) {
			for(String worldGroupName : wgConfigSec.getKeys(false)) {
				Utils.debugMessage("Loading world group: " + worldGroupName);
				List<String> worldNames = wgConfigSec.getStringList(worldGroupName);
				if(worldNames.isEmpty()) {
					Utils.debugMessage(worldGroupName + " is empty! Oh no!");
					continue;
				}
				Utils.debugMessage("Added worlds: " + worldNames.toString() + " for  WG: " + worldGroupName);
				WORLD_GROUPS.add(new WorldGroup(worldGroupName, worldNames));
			}
		}
		if(ENABLE_WORLD_GROUPS && WORLD_GROUPS.isEmpty()) {
			// log error, no world groups but feature is enabled.
			ENABLE_WORLD_GROUPS = false;
		}
		MAILTYPES_IGNORE_WORLD_GROUPS = config.getStringList("mail-types-that-ignore-world-groups");
		WORLD_BLACKLIST = config.getStringList("world-blacklist");
		/* Load general options */
		DISABLED_MAILTYPES = new ArrayList<String>();
		ConfigurationSection mtConfigSection = config.getConfigurationSection("enabled-mail-types");
		if(mtConfigSection != null) {
			for(String mailTypeNode : mtConfigSection.getKeys(false)) {
				if(!mtConfigSection.getBoolean(mailTypeNode, true)) {
					DISABLED_MAILTYPES.add(mailTypeNode);
					MailManager.getInstance().deregisterMailTypeByName(mailTypeNode);
				}
			}
		}

		String uuidValue = config.getString("use-uuids", "true");
		try {
			USE_UUIDS = Boolean.parseBoolean(uuidValue);
		} catch (Exception e) {
			USE_UUIDS = true;
			if(uuidValue.equalsIgnoreCase("always")) {
				FORCE_UUIDS = true;
			}
		}
		CHECK_FOR_UPDATES = config.getBoolean("update-checker.enabled", true);
		String autoDownloadSetting = config.getString("update-checker.auto-download", "all");
		AUTO_DOWNLOAD_TYPE = AutoDownloadType.ALL;
		if(autoDownloadSetting.equalsIgnoreCase("bugfix")) {
			AUTO_DOWNLOAD_TYPE = AutoDownloadType.BUGFIXES;
		}
		else if(autoDownloadSetting.equalsIgnoreCase("none") || autoDownloadSetting.equalsIgnoreCase("off") || autoDownloadSetting.equalsIgnoreCase("false")) {
			AUTO_DOWNLOAD_TYPE = AutoDownloadType.NONE;
		}

		/* Load localization options */
		DATE_FORMAT = config.getString("date-format", "MMM d, yyyy h:mm a");
		LOCALE_TAG = config.getString("locale-tag", "en-US");
		DEFAULT_TIMEZONE = config.getString("time-zone");
		List<String> zonesTrim = new ArrayList<String>();
		for(String zone : TimeZone.getAvailableIDs()) {
			if(zone.length() > 3) continue;
			zonesTrim.add(zone);
		}
		Collections.sort(zonesTrim);
		TIMEZONES = zonesTrim;
		/* User Settings */
		INBOX_SIZES = new HashMap<String, Integer>();
		ConfigurationSection inboxLimitsCS = config.getConfigurationSection("box-sizes");
		if(inboxLimitsCS != null) {
			for(String permGroup : inboxLimitsCS.getKeys(false)) {
				INBOX_SIZES.put(permGroup, inboxLimitsCS.getInt(permGroup, 50));
			}
		}
		if(!INBOX_SIZES.containsKey("default")) {
			INBOX_SIZES.put("default", 50);
		}
		UNREAD_NOTIFICATION_LOGIN = config.getBoolean("unread-mail-notifications.on-login", true);
		UNREAD_NOTIFICATION_ON_RECEIVE = config.getBoolean("unread-mail-notifications.on-recieve-mail", true);
		HARD_ENFORCE_INBOX_LIMIT = config.getBoolean("hard-enforce-inbox-limit", true);

		/* Mailbox Settings */
		ENABLE_MAILBOXES = config.getBoolean("enable-mailboxes", true);
		REQUIRE_MAILBOX = config.getBoolean("require-mailbox", true);
		MAILBOX_LIMITS = new HashMap<String, Integer>();

		ConfigurationSection mailboxLimitsCS = config.getConfigurationSection("mailbox-limits");
		if(mailboxLimitsCS != null) {
			for(String permGroup : mailboxLimitsCS.getKeys(false)) {
				MAILBOX_LIMITS.put(permGroup, mailboxLimitsCS.getInt(permGroup, 5));
			}
		}
		if(!MAILBOX_LIMITS.containsKey("default")) {
			MAILBOX_LIMITS.put("default", 5);
		}
	}

	public static boolean mailTypeIsDisabled(MailType mailType) {
		return mailTypeIsDisabled(mailType.getDisplayName());
	}

	public static boolean packagesAreEnabled() {
		return !mailTypeIsDisabled("package");
	}

	public static boolean mailTypeIsDisabled(String name) {
		for(String type : DISABLED_MAILTYPES) {
			if(type.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public static boolean playerIsInBlacklistedWorld(Player player) {
		return WORLD_BLACKLIST.contains(player.getWorld().getName());
	}

	@SuppressWarnings("deprecation")
	public static int getMaxInboxSizeForPlayer(String playerName) {
		if(PostalService.hasPermPlugin) {
			String primaryGroup = PostalService.permission.getPrimaryGroup("", playerName);
			if(primaryGroup != null) {
				if(INBOX_SIZES.containsKey(primaryGroup)) {
					return INBOX_SIZES.get(primaryGroup);
				}
			} else {
				if(ENABLE_DEBUG)
					PostalService.getPlugin().getLogger().warning("Could not get the primary group for player: " + playerName);
			}
		}
		return INBOX_SIZES.get("default");
	}

	@SuppressWarnings("deprecation")
	public static int getMailboxLimitForPlayer(String playerName) {
		if(PostalService.hasPermPlugin) {
			String primaryGroup = PostalService.permission.getPrimaryGroup("", playerName);
			if(primaryGroup != null) {
				if(MAILBOX_LIMITS.containsKey(primaryGroup)) {
					return MAILBOX_LIMITS.get(primaryGroup);
				}
			} else {
				if(ENABLE_DEBUG)
					PostalService.getPlugin().getLogger().warning("Could not get the primary group for player: " + playerName);
			}
		}
		return MAILBOX_LIMITS.get("default");
	}

	public static WorldGroup getCurrentWorldGroupForUser(User user) {
		Utils.debugMessage("Getting world group for " + user.getPlayerName());
		Player player = Utils.getPlayerFromIdentifier(user.getIdentifier());
		if(player != null && player.isOnline()) {
			World world = player.getWorld();
			if(world != null) {
				Utils.debugMessage(user.getPlayerName() + " found player. They are in world " + world.getName());
			} else {
				Utils.debugMessage(user.getPlayerName() + " found player. But their world is null?!");
			}
			return getWorldGroupFromWorld(player.getWorld());
		}
		return new WorldGroup("None", new ArrayList<String>());
	}

	public static WorldGroup getWorldGroupFromWorld(String worldName) {
		if(worldName != null) {
			Utils.debugMessage("Getting world group from world " + worldName);
		} else {
			Utils.debugMessage("Attempting to get worldgroup but the worldname is null?!");
		}
		if(!Config.ENABLE_WORLD_GROUPS)
			return new WorldGroup("None", new ArrayList<String>());
		for(WorldGroup worldGroup : WORLD_GROUPS) {
			if(worldGroup.hasWorld(worldName))
				return worldGroup;
		}
		return new WorldGroup("None", new ArrayList<String>());
	}

	public static WorldGroup getWorldGroupFromWorld(World world) {
		if(world != null) {
			Utils.debugMessage("Getting world group from world " + world.getName());
		} else {
			Utils.debugMessage("Attempting to get worldgroup but the world is null?!");
		}
		return getWorldGroupFromWorld(world.getName());
	}

	public static boolean containsMailTypesThatIgnoreWorldGroups() {
		if(MAILTYPES_IGNORE_WORLD_GROUPS == null || MAILTYPES_IGNORE_WORLD_GROUPS.isEmpty())
			return false;
		return true;
	}

	public static List<String> getMailTypesThatIgnoreWorldGroups() {
		return MAILTYPES_IGNORE_WORLD_GROUPS;
	}

	public static WorldGroup getWorldGroupFromGroupName(String string) {
		Utils.debugMessage("Getting world group by name: " + string);
		if(!Config.ENABLE_WORLD_GROUPS)
			return new WorldGroup("None", new ArrayList<String>());
		for(WorldGroup worldGroup : WORLD_GROUPS) {
			if(worldGroup.getName().equalsIgnoreCase(string)) {
				Utils.debugMessage("Found: " + worldGroup.getName());
				return worldGroup;
			}
		}
		Utils.debugMessage("None found!");
		return new WorldGroup("None", new ArrayList<String>());
	}
}
