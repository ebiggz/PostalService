package com.gmail.erikbigler.postalservice.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.mail.MailManager;

public class PlayerListener implements Listener {

	/*
	 * When a player drops a book after placing it on the Compose button:
	 * Wipe the book clean, cancel the drop.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		if(MailManager.getInstance().willDropBook.contains(event.getPlayer())) {
			event.getItemDrop().setItemStack(new ItemStack(Material.BOOK_AND_QUILL));
			event.setCancelled(true);
			MailManager.getInstance().willDropBook.remove(event.getPlayer());
		}
	}
}
