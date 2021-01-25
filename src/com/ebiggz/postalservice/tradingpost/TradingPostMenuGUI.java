package com.ebiggz.postalservice.tradingpost;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.apis.guiAPI.GUI;
import com.ebiggz.postalservice.apis.guiAPI.GUIManager;
import com.ebiggz.postalservice.apis.guiAPI.GUIUtils;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.screens.MainMenuGUI;
import com.ebiggz.postalservice.utils.Utils;


public class TradingPostMenuGUI implements GUI {
	
	private Inventory getInventory(Player player) {
		Inventory inventory = GUIUtils.generateInventory(9*3, "Trading Post Menu");
		if(TradeManager.getInstance().playerHasActiveTradeSession(player)) {
			String otherPerson = "";
			TradeSession session = TradeManager.getInstance().getPlayersActiveTradeSession(player);
			if(session.getInitiatorName().equals(player.getName())) {
				otherPerson = session.getInviteeName();
			} else {
				otherPerson = session.getInitiatorName();
			}
			ItemStack activeTradeButton = GUIUtils.createButton(
					new ItemStack(Material.PLAYER_HEAD, 1),
					"Current Session w/" + otherPerson,
					Arrays.asList(
							ChatColor.RED + "Left-Click to " + ChatColor.BOLD + "Open Trade Session",
							ChatColor.DARK_RED + "Shift+Right-Click to " + ChatColor.BOLD + "Cancel Trade"));
			inventory.setItem(4, activeTradeButton);
		} else {
			ItemStack createTradeButton = GUIUtils.createButton(
					new ItemStack(Material.PLAYER_HEAD, 1),
					ChatColor.YELLOW + "Start Trade Session",
					Arrays.asList(
							ChatColor.RED + "Click to " + ChatColor.BOLD + "Start A Trade Session"));
			inventory.setItem(4, createTradeButton);
		}
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 9; i < 18; i++) {
			inventory.setItem(i, seperator);
		}
		ItemStack back = GUIUtils.createButton(Material.CHEST, ChatColor.YELLOW + "Back To Mailbox", Arrays.asList(ChatColor.RED + "Click to return to mailbox"));
		inventory.setItem(22, back);
		return inventory;
	}

	@Override
	public Inventory createBaseInventory(Player player) {
		return getInventory(player);
	}
	
	public ItemStack[] loadContents(Player player) {
		return getInventory(player).getContents();
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		switch(clickedEvent.getSlot()) {
			case 4:
				if(TradeManager.getInstance().playerHasActiveTradeSession(whoClicked)) {
					TradeSession currentSession = TradeManager.getInstance().getPlayersActiveTradeSession(whoClicked);
					if(clickedEvent.getClick() == ClickType.LEFT) {
						GUIManager.getInstance().showGUI(currentSession, whoClicked);
					}
					else if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
						currentSession.cancelTrade();
					}
				} else {
					GUIManager.getInstance().showGUI(new TradeStarterGUI(), whoClicked);
				}
				break;
			case 22:
				GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(whoClicked), true), whoClicked);
				break;
			default:
				break;
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {
	}

	@Override
	public boolean ignoreForeignItems() {
		return false;
	}
}
