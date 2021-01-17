package com.ebiggz.postalservice.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.apis.InteractiveMessageAPI.FormattedText;
import com.ebiggz.postalservice.apis.InteractiveMessageAPI.InteractiveMessage;
import com.ebiggz.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement;
import com.ebiggz.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.ClickEvent;
import com.ebiggz.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.HoverEvent;
import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.mail.MailManager;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;

public class Utils {

	public static void debugMessage(String message) {
		if (Config.ENABLE_DEBUG)
			PostalService.getPlugin().getLogger().info("DEBUG: " + message);
	}
	
	public static ItemStack getPlayerHead(Player player) {
		ItemStack i = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) i.getItemMeta();
        im.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        i.setItemMeta(im);
        return i;
	}

	public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords) {
		if (str == null) {
			return null;
		}
		if (wrapLength < 1) {
			wrapLength = 1;
		}
		int inputLineLength = str.length();
		int offset = 0;
		StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

		while (inputLineLength - offset > wrapLength) {
			if (str.charAt(offset) == ' ') {
				offset++;
				continue;
			}
			int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

			if (spaceToWrapAt >= offset) {
				// normal case
				wrappedLine.append(str.substring(offset, spaceToWrapAt));
				wrappedLine.append(newLineStr);
				offset = spaceToWrapAt + 1;

			} else {
				// really long word or URL
				if (wrapLongWords) {
					// wrap really long word one line at a time
					wrappedLine.append(str.substring(offset, wrapLength + offset));
					wrappedLine.append(newLineStr);
					offset += wrapLength;
				} else {
					// do not wrap really long word, just extend beyond limit
					spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
					if (spaceToWrapAt >= 0) {
						wrappedLine.append(str.substring(offset, spaceToWrapAt));
						wrappedLine.append(newLineStr);
						offset = spaceToWrapAt + 1;
					} else {
						wrappedLine.append(str.substring(offset));
						offset = inputLineLength;
					}
				}
			}
		}

		// Whatever is left in line is short enough to just pass through
		wrappedLine.append(str.substring(offset));

		return wrappedLine.toString();
	}

	public static String capitalize(String str, char... delimiters) {
		int delimLen = delimiters == null ? -1 : delimiters.length;
		if (StringUtils.isEmpty(str) || delimLen == 0) {
			return str;
		}
		char[] buffer = str.toCharArray();
		boolean capitalizeNext = true;
		for (int i = 0; i < buffer.length; i++) {
			char ch = buffer[i];
			if (isDelimiter(ch, delimiters)) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer[i] = Character.toTitleCase(ch);
				capitalizeNext = false;
			}
		}
		return new String(buffer);
	}

	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(ch);
		}
		for (char delimiter : delimiters) {
			if (ch == delimiter) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getAllStartsWith(String key, List<String> list) {
		if(key == null || key.trim().isEmpty()) return list;
		List<String> filtered = new ArrayList<String>();
		for(String item : list) {
			if(item.toLowerCase().trim().startsWith(key.toLowerCase().trim())) {
				filtered.add(item);
			}
		}
		return filtered;
	}

	public static InteractiveMessage getUpdateAvailableMessage() {
		InteractiveMessage im = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(Phrases.ALERT_UPDATE_AVAILABLE.toPrefixedString()).setColor(Phrases.ALERT_UPDATE_AVAILABLE.getFirstColor()).setFormat(Phrases.ALERT_UPDATE_AVAILABLE.getFirstFormat())));
		im.addElement("\n");
		im.addElement(Phrases.PREFIX.toString() + " ");
		im.addElement(
				new InteractiveMessageElement(Phrases.UPDATE_BUTTON_VIEW_NOTES.toString(), HoverEvent.SHOW_TEXT, Phrases.UPDATE_BUTTON_VIEW_NOTES_HOVER.toString(), ClickEvent.OPEN_URL, PostalService.getUpdater().getLatestReleaseNotesLink()));
		im.addElement(" ");
		im.addElement(
				new InteractiveMessageElement(Phrases.UPDATE_BUTTON_DOWNLOAD.toString(), HoverEvent.SHOW_TEXT, Phrases.UPDATE_BUTTON_DOWNLOAD_HOVER.toString(), ClickEvent.RUN_COMMAND, "/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_DOWNLOAD.toString()));
		return im;
	}

	public static InteractiveMessage getUpdateDownloadedMessage() {
		InteractiveMessage im = new InteractiveMessage(new InteractiveMessageElement(new FormattedText(Phrases.ALERT_UPDATE_DOWNLOAD_SUCCESS.toPrefixedString()).setColor(Phrases.ALERT_UPDATE_DOWNLOAD_SUCCESS.getFirstColor()).setFormat(Phrases.ALERT_UPDATE_DOWNLOAD_SUCCESS.getFirstFormat())));
		im.addElement("\n");
		im.addElement(Phrases.PREFIX.toString() + " ");
		im.addElement(
				new InteractiveMessageElement(Phrases.UPDATE_BUTTON_VIEW_NOTES.toString(), HoverEvent.SHOW_TEXT, Phrases.UPDATE_BUTTON_VIEW_NOTES_HOVER.toString(), ClickEvent.OPEN_URL, PostalService.getUpdater().getLatestReleaseNotesLink()));
		return im;
	}

	public static InteractiveMessage getComposeMessage(boolean isReply, Player player) {
		InteractiveMessage im = new InteractiveMessage();
		if(isReply) {
			im.addElement(Phrases.REPLY_TEXT.toPrefixedString() + ": ");
		} else {
			im.addElement(Phrases.COMPOSE_TEXT.toPrefixedString() + ": ");
		}
		MailType[] types = MailManager.getInstance().getMailTypes();
		int count = 0;
		for(MailType type : types) {
			if(!PermissionHandler.playerCanMailType(type.getDisplayName().toLowerCase().trim(), player)) continue;
			if(count > 0) {
				im.addElement(", ", ChatColor.AQUA);
			}
			String attachArg = "";
			if(type.getAttachmentCommandArgument() != null && !type.getAttachmentCommandArgument().isEmpty()) {
				attachArg = " " + type.getAttachmentCommandArgument() + ":";
			}
			InteractiveMessageElement ime = new InteractiveMessageElement(
					new FormattedText(ChatColor.stripColor(type.getDisplayName()), ChatColor.AQUA),
					HoverEvent.SHOW_TEXT,
					new FormattedText(ChatColor.stripColor(type.getHoveroverDescription()), ChatColor.GOLD),
					ClickEvent.SUGGEST_COMMAND,
					"/" + Phrases.COMMAND_MAIL.toString() + " " + type.getDisplayName().toLowerCase() + " " + Phrases.COMMAND_ARG_TO.toString() + ": " + Phrases.COMMAND_ARG_MESSAGE.toString() + ":" + attachArg);
			im.addElement(ime);
			count++;
		}
		return im;
	}

	public static FancyMenu fancyHelpMenu(CommandSender player, String commandLabel) {
		FancyMenu fancyMenu = new FancyMenu(Phrases.HELPMENU_TITLE.toString(), commandLabel);
		fancyMenu.addText(Phrases.HELPMENU_TIP.toString());
		if(!Config.REQUIRE_MAILBOX) {
			if(PermissionHandler.playerHasPermission(Perm.MAIL_READ, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString(), Phrases.HELPMENU_MAIL_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			}
		}
		if(PermissionHandler.playerHasPermission(Perm.RELOAD, player, false)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_RELOAD.toString(), Phrases.HELPMENU_RELOAD_DESC.toString(), ClickEvent.RUN_COMMAND, null);
		}
		if(PermissionHandler.playerHasPermission(Perm.UPDATE, player, false)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_UPDATE.toString(), Phrases.HELPMENU_UPDATE_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_DOWNLOAD.toString(), Phrases.HELPMENU_DOWNLOAD_DESC.toString(), ClickEvent.RUN_COMMAND, null);
		}
		if(PermissionHandler.playerHasPermission(Perm.MAIL_READOTHER, player, false)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.HELPMENU_PLAYER_VARIABLE.toString(), Phrases.HELPMENU_READOTHER_DESC.toString(), ClickEvent.SUGGEST_COMMAND, "/" + Phrases.COMMAND_MAIL.toString() + " ");
		}
		if(PermissionHandler.playerHasPermission(Perm.HELP, player, false)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_HELP.toString(), Phrases.HELPMENU_HELP_DESC.toString(), ClickEvent.RUN_COMMAND, null);
		}
		if(PermissionHandler.playerCanMailSomething(player)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_COMPOSE.toString(), Phrases.HELPMENU_COMPOSE_DESC.toString(), ClickEvent.RUN_COMMAND, null);
		}
		if(PermissionHandler.playerHasPermission(Perm.MAIL_CHECK, player, false)) {
			fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_CHECK.toString(), Phrases.HELPMENU_CHECK_DESC.toString(), ClickEvent.RUN_COMMAND, null);
		}
		fancyMenu.addCommand("/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_TIMEZONE.toString() + " " + Phrases.HELPMENU_TIMEZONE_VARIABLE.toString(), Phrases.HELPMENU_TIMEZONE_DESC.toString(), ClickEvent.SUGGEST_COMMAND, "/" + Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_TIMEZONE.toString() + " ");
		if(Config.ENABLE_MAILBOXES) {
			if(PermissionHandler.playerHasPermission(Perm.MAILBOX_SET, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_SET.toString(), Phrases.HELPMENU_SET_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			}
			if(PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVE, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_REMOVE.toString(), Phrases.HELPMENU_REMOVE_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			}
			if(PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALL, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_REMOVEALL.toString(), Phrases.HELPMENU_REMOVEALL_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			}
			if(PermissionHandler.playerHasPermission(Perm.MAILBOX_REMOVEALLOTHER, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_REMOVEALL.toString() + " " + Phrases.HELPMENU_PLAYER_VARIABLE.toString(), Phrases.HELPMENU_REMOVEALLOTHER_DESC.toString(), ClickEvent.SUGGEST_COMMAND, "/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_REMOVEALL.toString() + " ");
			}
			if(PermissionHandler.playerHasPermission(Perm.MAILBOX_FIND, player, false)) {
				fancyMenu.addCommand("/" + Phrases.COMMAND_MAILBOX.toString() + " " + Phrases.COMMAND_ARG_FIND.toString(), Phrases.HELPMENU_FIND_DESC.toString(), ClickEvent.RUN_COMMAND, null);
			}
		}
		return fancyMenu;
	}

	public static Player getPlayerFromIdentifier(String identifier) {
		Player player;
		if (Config.USE_UUIDS) {
			player = Bukkit.getPlayer(UUID.fromString(identifier));
		} else {
			player = Bukkit.getPlayer(identifier);
		}
		return player;
	}

	public static boolean playerIsOnline(String identifier) {
		Player player = getPlayerFromIdentifier(identifier);
		return (player != null && player.isOnline());
	}

	public static void messagePlayerIfOnline(String identifier, String message) {
		Player player = getPlayerFromIdentifier(identifier);
		if (player != null && player.isOnline()) {
			player.sendMessage(message);
		}
	}

	public static int getPlayerOpenInvSlots(Player player) {
		Inventory inv = player.getInventory();
		ItemStack[] contents = inv.getContents();
		int count = 0;
		for (ItemStack content : contents) {
			if (content == null)
				count++;
		}
		return count;
	}

	public static void unreadMailAlert(User user, boolean onlyUnreadAlert) {
		int unread = user.getUnreadMailCount();
		if (unread == 0) {
			if (!onlyUnreadAlert) {
				messagePlayerIfOnline(user.getIdentifier(), Phrases.ALERT_NO_UNREAD_MAIL.toPrefixedString());
			}
		} else {
			messagePlayerIfOnline(user.getIdentifier(), Phrases.ALERT_UNREAD_MAIL.toPrefixedString().replace("%count%", Integer.toString(unread)));
		}
	}

	@SuppressWarnings("unchecked")
	public static Player[] getOnlinePlayers() {
		try {
			if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
				Collection<? extends Player> players = ((Collection<? extends Player>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
				players.toArray(new Player[players.size()]);
			} else {
				return ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
			}
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
		return new Player[0];
	}

	public static List<String> getNamesThatStartWith(String prefix) {
		List<String> matches = new ArrayList<String>();
		if(prefix == null || prefix.trim().isEmpty()) return matches;
		boolean prefixTo = false;
		if (prefix.startsWith(Phrases.COMMAND_ARG_TO.toString() + ":")) {
			prefix = prefix.replace(Phrases.COMMAND_ARG_TO.toString() + ":", "");
			prefixTo = true;
		}
		for (Player player : getOnlinePlayers()) {
			if (player.getName().startsWith(prefix)) {
				if (!player.getName().equals(prefix)) {
					if (prefixTo) {
						matches.add(Phrases.COMMAND_ARG_TO.toString() + ":" + player.getName());
					} else {
						matches.add(player.getName());
					}
				}
			}
		}
		return matches;
	}

	@SuppressWarnings("unchecked")
	public static String completeName(String playername) {
		try {
			if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class)
				for (Player onlinePlayer : ((Collection<? extends Player>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]))) {
					if (onlinePlayer.getName().toLowerCase().startsWith(playername.toLowerCase())) {
						return onlinePlayer.getName();
					}
				}
			else
				for (Player onlinePlayer : ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]))) {
					if (onlinePlayer.getName().toLowerCase().startsWith(playername.toLowerCase())) {
						return onlinePlayer.getName();
					}
				}
		} catch (NoSuchMethodException ex) {
		} // can never happen
		catch (InvocationTargetException ex) {
		} // can also never happen
		catch (IllegalAccessException ex) {
		} // can still never happen
		return null;
	}

	public static byte[] itemsToBytes(List<ItemStack> items) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
			boos.writeObject(items);
			boos.close();
		} catch (IOException ioexception) {
			if (Config.ENABLE_DEBUG)
				ioexception.printStackTrace();
		}

		return baos.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public static List<ItemStack> bytesToItems(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		Object backFromTheDead = null;
		try {
			BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
			backFromTheDead = bois.readObject();
			bois.close();
		} catch (IOException ioexception) {
			if (Config.ENABLE_DEBUG)
				ioexception.printStackTrace();
		} catch (ClassNotFoundException classNotFoundException) {
			if (Config.ENABLE_DEBUG)
				classNotFoundException.printStackTrace();
		}
		return (List<ItemStack>) backFromTheDead;
	}

	public static String locationToString(Location location) {
		return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName();
	}

	public static Location stringToLocation(String string) {
		String[] split = string.split(",");
		Utils.debugMessage("Attempting to find world: \"" + split[3].trim() + "\"");
		World world = Bukkit.getWorld(split[3].trim());
		if(world == null) {
			Utils.debugMessage("Unable to find world!! Here are the worlds I can find: ");
			String worlds = "";
			for(World w : Bukkit.getWorlds()) {
				worlds += "\"" + w.getName() + "\", ";
				if(split[3].trim().equals(w.getName().trim())) {
					world = w;
				}
			}
			Utils.debugMessage(worlds);
			if(world != null) {
				Utils.debugMessage("A manual match was made!! Found world: " + world.getName());
			} else {
				Utils.debugMessage("A match could not be found.");
			}
		} else {
			Utils.debugMessage("A match was made!! Found world: " + world.getName());
		}
		return new Location(Bukkit.getServer().getWorld(split[3].trim()), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
	}
}
