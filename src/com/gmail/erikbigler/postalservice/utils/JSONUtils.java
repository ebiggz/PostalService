package com.gmail.erikbigler.postalservice.utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSONUtils {

	@SuppressWarnings("unchecked")
	public static String packItems(List<ItemStack> items) {
		JSONArray jsonArray = new JSONArray();
		for(ItemStack is : items) {
			JSONObject obj = new JSONObject();

			obj.put("amount", new Integer(is.getAmount()));
			obj.put("durability", new Short(is.getDurability()));

			MaterialData md = is.getData();

			obj.put("material", md.getItemType().name());

			if (is.hasItemMeta()) {
				ItemMeta im = is.getItemMeta();

				if (im.hasLore()) {
					List<String> lores = im.getLore();

					JSONArray l1 = new JSONArray();

					for (String lore : lores) {
						l1.add(lore);
					}

					obj.put("lores", l1);
				}

				if (im.hasDisplayName())
					obj.put("displayName", im.getDisplayName());
				JSONObject o2;
				if (im.hasEnchants()) {
					JSONArray l2 = new JSONArray();

					Map<Enchantment, Integer> enchants = im.getEnchants();

					for (Enchantment enchant : enchants.keySet()) {
						o2 = new JSONObject();

						o2.put("enchantName", enchant.getName());
						o2.put("enchantPower", enchants.get(enchant));

						l2.add(o2);
					}

					obj.put("enchantments", l2);
				}

				if (md.getItemType() == Material.ENCHANTED_BOOK) {
					EnchantmentStorageMeta esm = (EnchantmentStorageMeta)is.getItemMeta();

					if (esm.hasStoredEnchants()) {
						JSONArray l2 = new JSONArray();

						Map<Enchantment, Integer> enchants = esm.getStoredEnchants();

						for (Enchantment enchant : enchants.keySet()) {
							o2 = new JSONObject();

							o2.put("enchantName", enchant.getName());
							o2.put("enchantPower", enchants.get(enchant));

							l2.add(o2);
						}

						obj.put("storedEnchants", l2);
					}
				}

			}
			jsonArray.add(obj);
		}
		return jsonArray.toJSONString();
	}

	public static List<ItemStack> unpackItems(String data) {
		JSONArray jsonItems = (JSONArray) JSONValue.parse(data);
		if (jsonItems == null) return null;
		return unpack(jsonItems);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<ItemStack> unpack(JSONArray jsonItems) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(Object itemO : jsonItems) {
			JSONObject obj = (JSONObject) itemO;
			int amount = ((Long)obj.get("amount")).intValue();
			short durability = ((Long)obj.get("durability")).shortValue();

			ItemStack is = new ItemStack(Material.getMaterial((String)obj.get("material")),
					amount, durability);

			ItemMeta im = is.getItemMeta();

			String displayName = (String)obj.get("displayName");

			if (displayName != null) {
				im.setDisplayName(displayName);
			}

			JSONArray l5 = (JSONArray)obj.get("lores");

			if (l5 != null) {
				im.setLore(l5);
			}

			JSONArray l1 = (JSONArray)obj.get("enchantments");

			if (l1 != null) {
				Iterator i = l1.iterator();

				while (i.hasNext()) {
					JSONObject j1 = (JSONObject)i.next();

					String enchantName = (String)j1.get("enchantName");
					int enchantPower = ((Long)j1.get("enchantPower")).intValue();

					Enchantment e = Enchantment.getByName(enchantName);

					im.addEnchant(e, enchantPower, true);
				}
			}
			if (is.getData().getItemType() == Material.ENCHANTED_BOOK) {
				EnchantmentStorageMeta esm = (EnchantmentStorageMeta)im;

				JSONArray k1 = (JSONArray)obj.get("storedEnchants");

				if (k1 != null) {
					Iterator i = k1.iterator();

					while (i.hasNext()) {
						JSONObject j1 = (JSONObject)i.next();

						String enchantName = (String)j1.get("enchantName");
						int enchantPower = ((Long)j1.get("enchantPower")).intValue();

						Enchantment e = Enchantment.getByName(enchantName);

						esm.addStoredEnchant(e, enchantPower, true);
					}
				}
				is.setItemMeta(esm);
			}
			else {
				is.setItemMeta(im);
			}
			items.add(is);
		}
		return items;
	}
}