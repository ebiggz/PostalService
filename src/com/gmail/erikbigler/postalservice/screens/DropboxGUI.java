package com.gmail.erikbigler.postalservice.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class DropboxGUI implements GUI {

	@Override
	public Inventory createInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9*5, player.getName() + "'s Drop Box");
		User user = UserFactory.getUser(player.getUniqueId());
		List<ItemStack> dbItems = user.getDropbox(Config.getWorldGroupFromWorld(player.getWorld().getName()));
		for(ItemStack item : dbItems) {
			inventory.addItem(item);
		}
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		List<String> lore = new ArrayList<String>();
		String[] wrappedMessage = Utils.wrap("You can place items from your inventory anywhere above the dotted line. Once you have, click the Compose Package button to the right. All items in your drop box are sent when you mail the package!", 30, "\n", true).split("\n");
		for(String line : wrappedMessage) {
			lore.add(ChatColor.WHITE + line);
		}
		ItemStack infoSign = GUIUtils.createButton(
				Material.SIGN,
				ChatColor.YELLOW +""+ ChatColor.UNDERLINE + "Drop Box Help",
				lore);
		inventory.setItem(39, infoSign);

		ItemStack mainMenu = GUIUtils.createButton(
				Material.BOOK_AND_QUILL,
				ChatColor.YELLOW +""+ ChatColor.BOLD + "Compose Package",
				Arrays.asList(
						ChatColor.RED + "Left-Click to " + ChatColor.BOLD + "Compose Package",
						ChatColor.RED + "Right-Click to " + ChatColor.BOLD + "Return"));
		inventory.setItem(40, mainMenu);
		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		ItemStack clickedItem = clickedEvent.getCurrentItem();
		if(clickedEvent.getSlot() < 27) {
			clickedEvent.setCancelled(false);
		} else {
			if(clickedEvent.getSlot() == 40) {
				if(clickedItem != null && clickedItem.getType() != Material.AIR) {
					if(clickedEvent.getClick() == ClickType.RIGHT) {
						GUIManager.getInstance().showGUI(new MainMenuGUI(), whoClicked);
					} else {
						whoClicked.closeInventory();
						String command = "tellraw {player} {\"text\":\"\",\"extra\":[{\"text\":\"[MPS] Click to compose: \",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"\"}},{\"text\":\"Package\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail package to: message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a package with items!\",\"color\":\"gold\"}]}}}]}";
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("{player}", whoClicked.getName()));
					}
				}
			}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(int i = 0; i < 26; i++) {
			ItemStack item = closeEvent.getInventory().getItem(i);
			if(item != null && item.getType() != Material.AIR) {
				items.add(item);
			}
		}
		UserFactory.getUser(whoClosed.getUniqueId()).saveDropbox(items, Config.getWorldGroupFromWorld(whoClosed.getWorld().toString()));
	}

	@Override
	public boolean ignoreForeignItems() {
		return true;
	}
}
