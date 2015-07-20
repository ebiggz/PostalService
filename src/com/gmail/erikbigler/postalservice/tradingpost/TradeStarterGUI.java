package com.gmail.erikbigler.postalservice.tradingpost;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;


public class TradeStarterGUI /*implements GUI*/ {

	/*@Override
	public Inventory createInventory(Player player) {
		List<String> players = TradeManager.getInstance().availablePlayers(player);
		int req = players.size() + 18;
		int multOf9 = 9;
		while(req > multOf9) {
			multOf9 += 9;
		}
		Inventory inventory = GUIUtils.generateInventory(multOf9, "Choose a player...");

		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		int barRowBegin = multOf9-18;
		int barRowEnd = multOf9-9;
		for(int i = barRowBegin; i < barRowEnd; i++) {
			inventory.setItem(i, seperator);
		}

		for(String playerName : players) {
			inventory.addItem(GUIUtils.createButton(
					Utils.getPlayerHeadItem(playerName),
					playerName,
					Arrays.asList(
							ChatColor.RED + "Click to Start Trade Session")));
		}

		inventory.setItem(multOf9-5,
				GUIUtils.createButton(
						Material.CHEST,
						ChatColor.YELLOW + "Main Menu",
						Arrays.asList(
								ChatColor.RED + "Click to Return")));

		return inventory;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		ItemStack clickedItem = clickedEvent.getCurrentItem();
		if(clickedItem != null && clickedItem.getType() != Material.AIR) {
			if(clickedItem.getType() == Material.SKULL_ITEM) {
				Player invitee = Bukkit.getPlayer(clickedItem.getItemMeta().getDisplayName());
				if(invitee != null && invitee.isOnline()) {
					TradeManager.getInstance().startTradeSession(whoClicked, invitee);
				}
			}
			else if(clickedItem.getType() == Material.CHEST) {
				GUIManager.getInstance().showGUI(new MailboxGUI(), whoClicked);
			}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {

	}

	@Override
	public boolean ignoreForeignItems() {
		return false;
	}*/

}
