package com.gmail.erikbigler.postalservice.apis.guiAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {

	//listens for when a player clicks in an inventory
	@EventHandler(priority=EventPriority.MONITOR)
	public void invClick(InventoryClickEvent e) {
		GUIManager gm = GUIManager.getInstance();
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(gm.playerHasGUIOpen(p)){
			GUI gui = gm.getPlayersCurrentGUI(p);
			if(e.getRawSlot() > e.getInventory().getSize()) return;
			e.setCancelled(true);
			gui.onInventoryClick(p, e);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void invDrag(InventoryDragEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) return;
		GUIManager gm = GUIManager.getInstance();
		Player p = (Player) e.getWhoClicked();
		if(gm.playerHasGUIOpen(p)){
			e.setCancelled(true);
		}
	}

	//listens for when a player closes an inventory
	@EventHandler(priority=EventPriority.MONITOR)
	public void invClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player)) return;
		GUIManager gm = GUIManager.getInstance();
		Player p = (Player) e.getPlayer();
		if(gm.playerHasGUIOpen(p)){
			GUI gui = gm.getPlayersCurrentGUI(p);
			gui.onInventoryClose(p, e);
			if(!gui.ignoreForeignItems()) {
				GUIUtils.removeForeignItems(p, gm.getGUIsCurrentInv(gui), e.getInventory());
			}
			gm.playerClosedGUI(p);
		}
	}
}