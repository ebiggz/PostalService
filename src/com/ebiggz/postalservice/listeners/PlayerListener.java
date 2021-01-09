package com.ebiggz.postalservice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.mail.MailManager;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.utils.Updater.UpdateResult;
import com.ebiggz.postalservice.utils.Utils;

public class PlayerListener implements Listener {

	/*
	 * When a player drops a book after placing it on the Compose button:
	 * Wipe the book clean, cancel the drop.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		if(MailManager.getInstance().willDropBook.contains(event.getPlayer())) {
			event.getItemDrop().setItemStack(new ItemStack(Material.WRITABLE_BOOK));
			event.setCancelled(true);
			MailManager.getInstance().willDropBook.remove(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.runTaskLaterAsynchronously(PostalService.getPlugin(), new Runnable() {
			private Player player;
			@Override
			public void run() {
				if(Config.UNREAD_NOTIFICATION_LOGIN) {
					User user = UserFactory.getUser(player);
					Utils.unreadMailAlert(user, true);
				}
				if(Config.CHECK_FOR_UPDATES) {
					if(PermissionHandler.playerHasPermission(Perm.UPDATE, player, false)) {
						UpdateResult result = PostalService.getUpdater().getResult();
						if(result == UpdateResult.UPDATE_AVAILABLE) {
							Utils.getUpdateAvailableMessage().sendTo(player);
						}
						else if(result == UpdateResult.SUCCESS) {
							Utils.getUpdateDownloadedMessage().sendTo(player);
						}
					}
				}
			}
			private Runnable init(Player player){
				this.player = player;
				return this;
			}
		}.init(event.getPlayer()), 20L);
	}
}
