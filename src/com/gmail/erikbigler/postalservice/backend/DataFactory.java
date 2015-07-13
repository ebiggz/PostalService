package com.gmail.erikbigler.postalservice.backend;

import java.util.UUID;

import com.gmail.erikbigler.postalservice.configs.ConfigManager;


public class DataFactory {

	public User getUser(String name) {
		if(ConfigManager.getInstance().USE_DATABASE) {
			return new DBUser(name);
		}
		return null;
	}

	public User getUser(UUID uuid) {
		if(ConfigManager.getInstance().USE_DATABASE) {
			return new DBUser(uuid);
		}
		return null;
	}

}
