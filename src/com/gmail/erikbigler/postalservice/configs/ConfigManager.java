package com.gmail.erikbigler.postalservice.configs;

import java.util.List;

public class ConfigManager {

	public boolean USE_DATABASE = true;
	public List<String> disabledMailTypes;

	protected ConfigManager() { /* exists to block instantiation */
	}

	private static ConfigManager instance = null;

	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	public void loadAllFiles() {
		Config.loadFile();
		Language.loadFile();
	}
}
