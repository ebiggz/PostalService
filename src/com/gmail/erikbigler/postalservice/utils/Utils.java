package com.gmail.erikbigler.postalservice.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.configs.Language.Phrases;

public class Utils {

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

	public static Player getPlayerFromIdentifier(String identifier) {
		Player player;
		if(Config.USE_UUIDS) {
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
		if(player != null && player.isOnline()) {
			player.sendMessage(message);
		}
	}

	public static void unreadMailAlert(User user, boolean onlyUnreadAlert) {
		int unread = user.getUnreadMailCount(Config.getCurrentWorldGroupForUser(user));
		if(unread == 0) {
			if(!onlyUnreadAlert) {
				messagePlayerIfOnline(user.getIdentifier(), Phrases.ALERT_NO_UNREAD_MAIL.toPrefixedString());
			}
		} else {
			messagePlayerIfOnline(user.getIdentifier(), Phrases.ALERT_UNREAD_MAIL.toPrefixedString().replace("%count%", Integer.toString(unread)));
		}
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
}
