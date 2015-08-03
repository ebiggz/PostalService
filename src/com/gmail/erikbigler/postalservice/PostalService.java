package com.gmail.erikbigler.postalservice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIListener;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.backend.database.Database;
import com.gmail.erikbigler.postalservice.backend.database.MySQL;
import com.gmail.erikbigler.postalservice.commands.MailCommands;
import com.gmail.erikbigler.postalservice.commands.MailTabCompleter;
import com.gmail.erikbigler.postalservice.commands.MailboxCommands;
import com.gmail.erikbigler.postalservice.commands.MailboxTabCompleter;
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
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.utils.Metrics;
import com.gmail.erikbigler.postalservice.utils.UUIDUtils;
import com.gmail.erikbigler.postalservice.utils.Updater;
import com.gmail.erikbigler.postalservice.utils.Updater.UpdateCallback;
import com.gmail.erikbigler.postalservice.utils.Updater.UpdateResult;
import com.gmail.erikbigler.postalservice.utils.Utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class PostalService extends JavaPlugin {

	private static Plugin plugin;
	private static Database database;
	private double serverVersion;
	private static Updater updater;
	private BukkitRunnable updateCheckTask;
	public static Economy economy = null;
	public static Permission permission = null;
	public static boolean vaultEnabled = false;
	public static boolean hasPermPlugin = false;
	public static boolean hasEconPlugin = false;
	private static int projectId = 93931;
	private static File file;

	/**
	 * Called when PostalService is being enabled
	 */
	@Override
	public void onEnable() {

		plugin = this;
		file = getFile();
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
		 * Register permissions
		 */

		PermissionHandler.registerPermissions();

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

		this.registerCommand("mail", Phrases.COMMAND_MAIL.toString(), "m", "ps", "postalservice");
		getCommand("mail").setExecutor(new MailCommands());
		getCommand("mail").setTabCompleter(new MailTabCompleter());

		this.registerCommand("mailbox",Phrases.COMMAND_MAILBOX.toString(), "mb");
		getCommand("mailbox").setExecutor(new MailboxCommands());
		getCommand("mailbox").setTabCompleter(new MailboxTabCompleter());

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

		scheduleTasks();

		//submit metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the metrics :-(
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
				getLogger().warning("No plugin to handle currency. Payment mail type will be not be available!");
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

	public static Updater getUpdater() {
		return updater;
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

	public int getProjectID() {
		return projectId;
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

	private void scheduleTasks() {
		//cancel any previously set tasks
		cancelTasks();

		//schedule new ones
		updateCheckTask = new UpdateCheckTask(this);
		updateCheckTask.runTaskTimerAsynchronously(this, 40, 432000);
	}

	private void cancelTasks() {
		if(updateCheckTask != null) {
			updateCheckTask.cancel();
		}
	}

	public static void manualUpdateCheck(final CommandSender sender) {
		updater = new Updater(plugin, projectId, file, Updater.UpdateType.NO_DOWNLOAD, new UpdateCallback() {
			@Override
			public void onFinish(Updater updater) {
				if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
					Utils.getUpdateAvailableMessage().sendTo(sender);
				} else {
					sender.sendMessage(Phrases.ERORR_UPDATE_DOWNLOAD_FAIL.toPrefixedString());
				}
			}

		}, true);
	}

	public static void downloadUpdate(final CommandSender sender) {
		if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			updater = new Updater(plugin, projectId, file, Updater.UpdateType.NO_VERSION_CHECK, new UpdateCallback() {
				@Override
				public void onFinish(Updater updater) {
					if(updater.getResult() == UpdateResult.SUCCESS) {
						sender.sendMessage(Phrases.ALERT_UPDATE_DOWNLOAD_SUCCESS.toPrefixedString());
					} else {
						sender.sendMessage(Phrases.ERORR_UPDATE_DOWNLOAD_FAIL.toPrefixedString());
					}
				}

			}, true);
		}
	}

	/** Functions for registering command aliases in code. */

	private void registerCommand(String... aliases) {
		PluginCommand command = getCommand(aliases[0], this);
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register("", command);
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

	//knownCommands

	private class UpdateCheckTask extends BukkitRunnable {

		PostalService plugin;

		private UpdateCheckTask(PostalService plugin) {
			this.plugin = plugin;
		}

		@Override
		public void run() {
			if(Config.CHECK_FOR_UPDATES) {
				plugin.getLogger().info("Checking for updates...");
				switch(Config.AUTO_DOWNLOAD_TYPE) {
				case BUGFIXES:
					updater = new Updater(plugin, plugin.getProjectID(), plugin.getFile(), Updater.UpdateType.BUGFIX_ONLY, true);
					break;
				case NONE:
					updater = new Updater(plugin, plugin.getProjectID(), plugin.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
					break;
				default:
					updater = new Updater(plugin, plugin.getProjectID(), plugin.getFile(), Updater.UpdateType.DEFAULT, true);
					break;
				}
				switch(updater.getResult()) {
				case DISABLED:
					plugin.getLogger().info("Update checking has been disabled in the config.");
					plugin.updateCheckTask.cancel();
					break;
				case NO_UPDATE:
					plugin.getLogger().info("You are up to date!");
					break;
				case SUCCESS:
					plugin.getLogger().info("An update was succussfully downloaded and will be available after the next server restart.");
					plugin.getLogger().info("Read the update notes here: " + updater.getLatestReleaseNotesLink());
					break;
				case UPDATE_AVAILABLE:
					plugin.getLogger().info("There is a new version available! (New version: " + updater.getLatestName() + " Current version: " + plugin.getDescription().getVersion() + ")");
					plugin.getLogger().info("Read the update notes here: " + updater.getLatestReleaseNotesLink());
					plugin.getLogger().info("Or type \"/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_DOWNLOAD.toString() + "\" to download the update now.");
					break;
				default:
					break;
				}
			}
		}
	}
}
