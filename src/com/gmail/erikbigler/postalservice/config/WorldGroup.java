package com.gmail.erikbigler.postalservice.config;

import java.util.List;

public class WorldGroup {

	private String groupName;
	private List<String> worldNames;

	public WorldGroup(String groupName, List<String> worldNames) {
		this.groupName = groupName;
		this.worldNames = worldNames;
	}

	public String getName() {
		return groupName;
	}

	public boolean hasWorld(String worldName) {
		return worldNames.contains(worldName);
	}
}
