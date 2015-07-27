package com.gmail.erikbigler.postalservice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIListener;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.backend.database.Database;
import com.gmail.erikbigler.postalservice.backend.database.MySQL;
import com.gmail.erikbigler.postalservice.commands.MailCommands;
import com.gmail.erikbigler.postalservice.commands.MailTabCompleter;
import com.gmail.erikbigler.postalservice.commands.MailboxCommands;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.listeners.MailboxListener;
import com.gmail.erikbigler.postalservice.listeners.PlayerListener;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Experience;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Letter;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Package;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Payment;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.utils.UUIDUtils;
import com.gmail.erikbigler.postalservice.utils.Updater;
import com.gmail.erikbigler.postalservice.utils.Utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class PostalService extends JavaPlugin {

	private static Plugin plugin;
	private static Database database;
	private double serverVersion;
	public static Updater updater;
	public static Economy economy = null;
	public static Permission permission = null;
	public static boolean vaultEnabled = false;
	public static boolean hasPermPlugin = false;
	public static boolean hasEconPlugin = false;

	/** Called when PostalService is being enabled */
	@Override
	public void onEnable() {

		plugin = this;
		new Utils();

		/*
		 * Check Server version
		 */
		String vString = getVersion().replace("v", "");
		serverVersion = 0;
		if(!vString.isEmpty()) {
			String[] array = vString.split("_");
			serverVersion = Double.parseDouble(array[0] + "." + array[1]);
		}
		if(serverVersion <= 1.6) {
			getLogger().severe("Sorry! PostalService is compatible with Bukkit 1.7 and above.");
			Bukkit.getPluginManager().disablePlugin(this);
		}

		/*
		 * Check for and setup vault
		 */
		if(setupVault()) {
			vaultEnabled = true;
		}

		/*
		 * Register listeners
		 */
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
		Bukkit.getPluginManager().registerEvents(new MailboxListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

		/*
		 * Load configs
		 */
		Config.loadFile();
		Language.loadFile();
		UUIDUtils.loadFile();

		/*
		 * Register built in MailTypes
		 */

		getMailManager().registerMailType(new Letter());
		getMailManager().registerMailType(new Experience());
		getMailManager().registerMailType(new Package());
		if(vaultEnabled && hasEconPlugin) {
			getMailManager().registerMailType(new Payment());
		}

		/*
		 * Register commands
		 */

		this.registerCommand("mail", Phrases.COMMAND_MAIL.toString());
		getCommand("mail").setExecutor(new MailCommands());
		getCommand("mail").setTabCompleter(new MailTabCompleter());
		getCommand(Phrases.COMMAND_MAIL.toString()).setExecutor(new MailCommands());

		this.registerCommand("mailbox", Phrases.COMMAND_MAILBOX.toString());
		getCommand("mailbox").setExecutor(new MailboxCommands());
		getCommand(Phrases.COMMAND_MAILBOX.toString()).setExecutor(new MailboxCommands());

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

		if(!getServer().getOnlineMode()) {
			if(!Config.FORCE_UUIDS) {
				Config.USE_UUIDS = false;
				getLogger().warning("UUIDs are enabled but the server is running in Offline Mode. UUIDs have been disabled. To force the use of UUIDs while in Offline mode, set \"use-uuids\" to \"always\" in the config.");
			}
		}

		MailboxManager.getInstance().loadMailboxes();

		if(Config.CHECK_FOR_UPDATES) {
			switch(Config.AUTO_DOWNLOAD_TYPE) {
			case BUGFIXES:
				updater = new Updater(this, 71726, this.getFile(), Updater.UpdateType.BUGFIX_ONLY, true);
				break;
			case NONE:
				break;
			default:
				updater = new Updater(this, 71726, this.getFile(), Updater.UpdateType.DEFAULT, true);
				break;

			}
			//start timed checker
		}

		getLogger().info("Enabled!");
	}

	/** Called when PostalService is being disabled */
	@Override
	public void onDisable() {
		//Make sure all open GUI inventories are closed when disabled. Otherwise, players would be able to access items during a reload.
		GUIManager.getInstance().closeAllGUIs();
		getLogger().info("Disabled!");
	}

	private boolean setupVault() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if(vault != null) {
			getLogger().info("Hooked into Vault!");
			if(!setupEconomy()) {
				getLogger().warning("No plugin to handle currency. Payment mail type will be not be available if enabled!");
			}
			if(!setupPermissions()) {
				getLogger().warning("No plugin to handle permission groups. Permission group settings will be ignored!");
			}
			return true;
		} else {
			getLogger().warning("Vault plugin not found. The Payment mailtype and permission group features will be not be functional!");
			return false;
		}
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economyProvider != null) {
			hasEconPlugin = true;
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if(permissionProvider != null) {
			if(permissionProvider.getProvider().hasGroupSupport()) {
				hasPermPlugin = true;
				permission = permissionProvider.getProvider();
			}
		}
		return (permission != null);
	}

	public boolean loadDatabase() {
		reloadConfig();
		database = new MySQL(this, getConfig().getString("database.host", "127.0.0.1"), getConfig().getString("database.port", "3306"), getConfig().getString("database.database", "someName"), getConfig().getString("database.user", "user"), getConfig().getString("database.password", "password"));
		try {
			database.openConnection();
			return database.checkConnection();
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG)
				e.printStackTrace();
			return false;
		}
	}

	/** @return the class that handles MailTypes */
	public static MailManager getMailManager() {
		return MailManager.getInstance();
	}

	/** @return the class that handles Mailboxes */
	public static MailboxManager getMailboxManager() {
		return MailboxManager.getInstance();
	}

	/** @return a link to the main class instance */
	public static Plugin getPlugin() {
		return plugin;
	}

	/** @return the database connection */
	public static Database getPSDatabase() {
		return database;
	}

	/** Determines the version string used by Craftbukkit's safeguard (e.g.
	 * 1_7_R4).
	 *
	 * @return the version string used by Craftbukkit's safeguard */
	private static String getVersion() {
		String[] array = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
		if(array.length == 4)
			return array[3] + ".";
		return "";
	}

	/** Functions for registering command aliases in code. */

	private void registerCommand(String... aliases) {
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
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return command;
	}

	private static CommandMap getCommandMap() {
		CommandMap commandMap = null;
		try {
			if(Bukkit.getPluginManager() instanceof SimplePluginManager) {
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);

				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG)
				e.printStackTrace();
		}
		return commandMap;
	}
}
