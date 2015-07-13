package com.gmail.erikbigler.postalservice.apis.guiAPI;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class GUIUtils {

	// Returns an inventory automatically sized based on a given amount of needed slots
	public static Inventory generateInventory(int neededSlots, String title) {
		int multOf9 = 9;
		while(neededSlots > multOf9) {
			multOf9 += 9;
		}
		return Bukkit.createInventory(null, multOf9, title);
	}

	// Returns an ItemStack with the given name and lore
	public static ItemStack createButton(Material icon, String name, List<String> lore) {
		ItemStack item = new ItemStack(icon);
		ItemMeta im = item.getItemMeta();
		if(name != null) {
			im.setDisplayName(name);
		}
		if(lore != null) {
			im.setLore(lore);
		}
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack createButton(ItemStack item, String name, List<String> lore) {
		ItemMeta im = item.getItemMeta();
		if(name != null) {
			im.setDisplayName(name);
		}
		if(lore != null) {
			im.setLore(lore);
		}
		item.setItemMeta(im);
		return item;
	}

	public static void removeForeignItems(Player viewer, List<ItemStack> original, Inventory afterClose) {
		for(ItemStack item : afterClose.getContents()) {
			if(item == null) continue;
			if(original.contains(item)) continue;
			viewer.getWorld().dropItemNaturally(viewer.getLocation(), item);
			afterClose.remove(item);
		}
	}
}
