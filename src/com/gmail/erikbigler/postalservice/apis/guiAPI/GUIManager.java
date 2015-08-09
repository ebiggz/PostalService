package com.gmail.erikbigler.postalservice.apis.guiAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.erikbigler.postalservice.PostalService;


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
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.runTaskAsynchronously(PostalService.getPlugin(), new Runnable() {
			private GUI gui;
			private Player player;
			@Override
			public void run() {
				Inventory inv = gui.createInventory(player);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.runTask(PostalService.getPlugin(), new Runnable() {
					private Inventory inv;
					@Override
					public void run() {
						player.closeInventory();
						openGUIs.put(player, gui);
						guiInvs.put(gui, Arrays.asList(inv.getContents()));
						player.openInventory(inv);
					}
					public Runnable init(Inventory inv) {
						this.inv = inv;
						return this;
					}
				}.init(inv));
			}
			public Runnable init(GUI gui, Player player) {
				this.gui = gui;
				this.player = player;
				return this;
			}
		}.init(gui, player));

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

	public void setGUIInv(GUI gui, List<ItemStack> items) {
		if(guiInvs.containsKey(gui)) {
			guiInvs.remove(gui);
			guiInvs.put(gui, items);
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
