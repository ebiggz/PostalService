package com.gmail.erikbigler.postalservice.apis.guiAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class GUIManager {

	//storage for players in guis
	private HashMap<Player,GUI> openGUIs = new HashMap<Player,GUI>();
	private HashMap<GUI,List<ItemStack>> guiInvs = new HashMap<GUI,List<ItemStack>>();

	protected GUIManager() { /*exists to block instantiation*/ }
	private static GUIManager instance = null;
	public static GUIManager getInstance() {
		if(instance == null) {
			instance = new GUIManager();
		}
		return instance;
	}

	//present a gui on players screen
	public void showGUI(GUI gui, Player player) {
		if(gui == null || player == null) return;
		player.closeInventory();
		openGUIs.put(player, gui);
		Inventory inv = gui.createInventory(player);
		guiInvs.put(gui, Arrays.asList(inv.getContents()));
		player.openInventory(inv);
	}

	//used by GUIListener to remove players from GUI storage
	public void playerClosedGUI(Player player) {
		guiInvs.remove(getPlayersCurrentGUI(player));
		openGUIs.remove(player);
	}

	public List<ItemStack> getGUIsCurrentInv(GUI gui) {
		if(guiInvs.containsKey(gui)) {
			return guiInvs.get(gui);
		} else {
			return null;
		}
	}

	public GUI getPlayersCurrentGUI(Player player) {
		if(openGUIs.containsKey(player)) {
			return openGUIs.get(player);
		} else {
			return null;
		}
	}

	public void closeAllGUIs() {
		for(Player player : openGUIs.keySet()) {
			player.closeInventory();
		}
	}


	public boolean playerHasGUIOpen(Player player) {
		return (openGUIs.containsKey(player));
	}
}
