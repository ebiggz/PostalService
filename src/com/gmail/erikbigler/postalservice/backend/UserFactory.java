package com.gmail.erikbigler.postalservice.backend;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.gmail.erikbigler.postalservice.config.Config;

public class UserFactory {

	public static User getUser(String name) {
		if (Config.USE_DATABASE) {
			return new DBUser(name);
		}
		return null;
	}

	public static User getUser(Player player) {
		if (Config.USE_DATABASE) {
			if (Config.USE_UUIDS) {
				return new DBUser(player.getUniqueId());
			} else {
				return new DBUser(player.getName());
			}
		}
		return null;
	}

	public static User getUserFromIdentifier(String identifier) {
		if (Config.USE_DATABASE) {
			if (Config.USE_UUIDS) {
				return new DBUser(UUID.fromString(identifier));
			} else {
				return new DBUser(identifier);
			}
		}
		return null;
	}
}
