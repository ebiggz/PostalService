package com.gmail.erikbigler.postalservice.utils;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.ConfigAccessor;

public class UUIDUtils {

	public static UUID findUUID(String playerName) {
		System.out.println(playerName);
		Utils.debugMessage("Attemping to get uuid from online player... ");
		// if the player is online, get the uuid from that
		Player player = Bukkit.getPlayerExact(playerName);
		if (player != null) {
			return player.getUniqueId();
		}
		Utils.debugMessage("Player is offline. Attempting to get uuid from local cache...");
		// if not, check to see if the uuid has been cached locally
		UUID cachedID = searchCacheForID(playerName);
		if (cachedID != null) {
			return cachedID;
		}

		Utils.debugMessage("Cache is empty. Attempting to get uuid from Mojang server...");
		// as a last resort, attempt to contact Mojang for the UUID.
		try {
			return UUIDFetcher.getUUIDOf(playerName);
		} catch (Exception e) {
			return null;
		}
	}

	public static String findPlayerName(UUID uuid) {
		Utils.debugMessage("Attemping to get player name from online player... ");
		// if the player is online, get the name from that
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			System.out.println(player.getName());
			return player.getName();
		}
		Utils.debugMessage("Player is offline. Attempting to get name from local cache...");
		// if not, check to see if the name has been cached locally
		String cachedName = searchCacheForName(uuid);
		if (cachedName != null) {
			System.out.println(cachedName);
			return cachedName;
		}

		Utils.debugMessage("Cache is empty. Attempting to get name from Mojang server...");
		// as a last resort, attempt to contact Mojang for the name.
		try {
			return NameFetcher.getNameOf(uuid);
		} catch (Exception e) {
			return null;
		}
	}

	public static String searchCacheForName(UUID id) {
		ConfigAccessor uuidCache = new ConfigAccessor("data" + File.separator + "UUIDCache.yml");
		if (uuidCache.getConfig().contains(id.toString())) {
			return uuidCache.getConfig().getString(id.toString());
		}
		return null;
	}

	public static UUID searchCacheForID(String playerName) {
		ConfigAccessor uuidCache = new ConfigAccessor("data" + File.separator + "UUIDCache.yml");
		if (uuidCache.getConfig().contains(playerName)) {
			return UUID.fromString(uuidCache.getConfig().getString(playerName));
		}
		return null;
	}

	public static void saveKnownNameAndUUID(String playerName, UUID id) {
		ConfigAccessor uuidCache = new ConfigAccessor("data" + File.separator + "UUIDCache.yml");
		uuidCache.getConfig().set(id.toString(), playerName);
		uuidCache.getConfig().set(playerName, id.toString());
		uuidCache.saveConfig();
	}

	public static void loadFile() {
		PluginManager pm = PostalService.getPlugin().getServer().getPluginManager();
		String pluginFolder = PostalService.getPlugin().getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		String playerFolder = pluginFolder + File.separator + "data";
		(new File(playerFolder)).mkdirs();
		File playerDataFile = new File(playerFolder, "UUIDCache.yml");
		ConfigAccessor playerData = new ConfigAccessor("data" + File.separator + "UUIDCache.yml");

		if (!playerDataFile.exists()) {
			try {
				playerData.saveDefaultConfig();
			} catch (Exception e) {
				Bukkit.getLogger().log(Level.SEVERE, "Exception while loading PostalService/data/UUIDCache.yml", e);
				pm.disablePlugin(PostalService.getPlugin());
			}
			return;
		} else {
			try {
				playerData.getConfig().options().header("This file caches playername/uuid combos so PostalService doesn't have to contact Mojang servers as often.");
				playerData.getConfig().options().copyHeader();
				playerData.reloadConfig();
			} catch (Exception e) {
				Bukkit.getLogger().log(Level.SEVERE, "Exception while loading PostalService/data/UUIDCache.yml", e);
				pm.disablePlugin(PostalService.getPlugin());
			}
		}
	}
}
