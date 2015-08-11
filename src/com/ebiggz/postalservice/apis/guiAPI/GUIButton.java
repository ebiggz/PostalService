package com.ebiggz.postalservice.apis.guiAPI;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class GUIButton {

	private ItemStack base;
	private String name;
	private List<String> lore;
	private boolean isGlowing = false;

	public GUIButton(Material icon, String name, List<String> lore) {
		this.base = new ItemStack(icon);
		this.name = name;
		this.lore = lore;
	}

	public GUIButton(Material icon, String name, List<String> lore, boolean shouldGlow) {
		this.base = new ItemStack(icon);
		this.name = name;
		this.lore = lore;
		this.isGlowing = shouldGlow;
	}

	public GUIButton(ItemStack base, String name, List<String> lore) {
		this.base = base;
		this.name = name;
		this.lore = lore;
	}

	public void setBaseIcon(ItemStack icon) {
		this.base = icon;
	}

	public void setBaseIcon(Material icon) {
		base.setType(icon);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getLore() {
		return this.lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public boolean isGlowing() {
		return this.isGlowing;
	}

	public void shouldGlow(boolean shouldGlow) {
		this.isGlowing = shouldGlow;
	}

	public ItemStack toItemStack() {
		ItemStack item = base.clone();
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		if(isGlowing) {
			im.addEnchant(Enchantment.DURABILITY, 1, true);
		}
		item.setItemMeta(im);
		return item;
	}
}
