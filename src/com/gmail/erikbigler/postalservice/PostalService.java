package com.gmail.erikbigler.postalservice;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIListener;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.configs.Language;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Letter;
import com.gmail.erikbigler.postalservice.utils.Utils;
import com.mythicacraft.voteroulette.utils.database.Database;
import com.mythicacraft.voteroulette.utils.database.MySQL;

public class PostalService extends JavaPlugin {

	private static PostalService plugin;
	private static Database database;
	private double serverVersion;

	/**
	 * Called when the PostalService is enabled
	 */
	public void onEnable() {

		plugin = this;
		new Utils();

		/*
		 * Check Server version
		 */
		String vString = getVersion().replace("v", "");
		serverVersion = 0;
		if (!vString.isEmpty()) {
			String[] array = vString.split("_");
			serverVersion = Double.parseDouble(array[0] + "." + array[1]);
		}
		if (serverVersion <= 1.6) {
			getLogger().severe("Sorry! PostalService is compatible with Bukkit 1.7 and above.");
			Bukkit.getPluginManager().disablePlugin(this);
		}

		/*
		 * Register MailTypes
		 */

		getMailManager().registerMailType(new Letter());
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);

		/*
		 * Load Configs
		 */
		Config.loadFile();
		Language.loadFile();

		/*
		 * Connect to database
		 */
		loadDatabase();

	}

	/**
	 * Called when the PostalService is disabled
	 */
	public void onDisable() {

	}

	public void loadDatabase() {
		reloadConfig();
		database = new MySQL(this, getConfig().getString("database.host", "127.0.0.1"), getConfig().getString("database.port", "3306"), getConfig().getString("database.database", "someName"), getConfig().getString("database.user", "user"), getConfig().getString("database.password", "password"));
		try {
			database.openConnection();
			if (!database.checkConnection()) {
				getLogger().severe("Unable to connect to the database! Please check your database settings and try again.");
				Bukkit.getPluginManager().disablePlugin(this);
			} else {
				getLogger().info("Sucessfully connected to the database!");
			}
		} catch (Exception e) {
			getLogger().severe("Unable to connect to the database! Please check your database settings and try again.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	/**
	 * @return the class that handles mailtypes
	 */
	public static MailManager getMailManager() {
		return MailManager.getInstance();
	}

	/**
	 * @return the class that creates Users
	 */
	public static UserFactory getUserFactory() {
		return new UserFactory();
	}

	/**
	 * @return a link to the main class instance
	 */
	public static PostalService getPlugin() {
		return plugin;
	}

	/**
	 * Determines the version string used by Craftbukkit's safeguard (e.g.
	 * 1_7_R4).
	 * 
	 * @return the version string used by Craftbukkit's safeguard
	 */
	private static String getVersion() {
		String[] array = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
		if (array.length == 4)
			return array[3] + ".";
		return "";
	}

}
