package com.ebiggz.postalservice.utils;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Library to find a name by uuid.
 * @author evilmidget38
 */
public class NameFetcher implements Callable<Map<UUID, String>> {

	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
	private final JsonParser jsonParser = new JsonParser();
	private final List<UUID> uuids;
	public NameFetcher(List<UUID> uuids) {
		this.uuids = ImmutableList.copyOf(uuids);
	}

	@Override
	public Map<UUID, String> call() throws Exception {
		Map<UUID, String> uuidStringMap = new HashMap<UUID, String>();
		for (UUID uuid: uuids) {
			HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL+uuid.toString().replace("-", "")).openConnection();
			JsonObject response = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
			String name = response.get("name").getAsString();
			if (name == null) {
				continue;
			}
			String cause = response.get("cause").getAsString();
			String errorMessage = response.get("errorMessage").getAsString();
			if (cause != null && cause.length() > 0) {
				throw new IllegalStateException(errorMessage);
			}
			uuidStringMap.put(uuid, name);
		}
		return uuidStringMap;
	}

	public static String getNameOf(UUID uuid) throws Exception {
		return new NameFetcher(Arrays.asList(uuid)).call().get(uuid);
	}
}