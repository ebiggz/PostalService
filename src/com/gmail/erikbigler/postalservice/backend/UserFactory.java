package com.gmail.erikbigler.postalservice.backend;

import java.util.UUID;

import com.gmail.erikbigler.postalservice.configs.Config;

public class UserFactory {

	public static User getUser(String name) {
		if (Config.USE_DATABASE) {
			return new DBUser(name);
		}
		return null;
	}

	public static User getUser(UUID uuid) {
		if (Config.USE_DATABASE) {
			return new DBUser(uuid);
		}
		return null;
	}

}
