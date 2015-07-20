package com.gmail.erikbigler.postalservice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIListener;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.commands.Commands;
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.configs.Language;
import com.gmail.erikbigler.postalservice.configs.Language.Phrases;
import com.gmail.erikbigler.postalservice.configs.UUIDUtils;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Experience;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Letter;
import com.gmail.erikbigler.postalservice.utils.Utils;
import com.gmail.erikbigler.postalservice.utils.database.Database;
import com.gmail.erikbigler.postalservice.utils.database.MySQL;

public class PostalService extends JavaPlugin {

	//private Plugin p;
	private static PostalService plugin;
	private static Database database;
	private double serverVersion;

	/**
	 * Called when PostalService is being enabled
	 */
	@Override
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
		 * Register built in MailTypes
		 */

		getMailManager().registerMailType(new Letter());
		getMailManager().registerMailType(new Experience());

		/*
		 * Register listeners
		 */
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);

		/*
		 * Load configs
		 */
		Config.loadFile();
		Language.loadFile();
		UUIDUtils.loadFile();

		/*
		 * Register commands
		 */

		this.registerCommand("mail", Phrases.COMMAND_MAIL.toString());
		getCommand("mail").setExecutor(new Commands());
		getCommand(Phrases.COMMAND_MAIL.toString()).setExecutor(new Commands());

		/*
		 * Connect to database
		 */
		if(!loadDatabase()) {
			getLogger().severe("Unable to connect to the database! Please check your database settings and try again.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		} else {
			getLogger().info("Sucessfully connected to the database!");
		}

		getLogger().info("Enabled!");

	}

	/**
	 * Called when PostalService is being disabled
	 */
	@Override
	public void onDisable() {
		getLogger().info("Disabled!");
	}

	public boolean loadDatabase() {
		reloadConfig();
		database = new MySQL(this, getConfig().getString("database.host", "127.0.0.1"), getConfig().getString("database.port", "3306"), getConfig().getString("database.database", "someName"), getConfig().getString("database.user", "user"), getConfig().getString("database.password", "password"));
		try {
			database.openConnection();
			return database.checkConnection();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return the class that handles MailTypes
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
	 * @return the database connection
	 */
	public static Database getPSDatabase() {
		return database;
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

	public void registerCommand(String... aliases) {
		PluginCommand command = getCommand(aliases[0], this);
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}

	private static PluginCommand getCommand(String name, Plugin plugin) {
		PluginCommand command = null;

		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);

			command = c.newInstance(name, plugin);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return command;
	}

	private static CommandMap getCommandMap() {
		CommandMap commandMap = null;

		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);

				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return commandMap;
	}

}
