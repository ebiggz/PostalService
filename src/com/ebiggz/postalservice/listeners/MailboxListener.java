package com.ebiggz.postalservice.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.apis.guiAPI.GUIManager;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.events.PlayerOpenMailMenuEvent;
import com.ebiggz.postalservice.exceptions.MailboxException;
import com.ebiggz.postalservice.mailbox.Mailbox;
import com.ebiggz.postalservice.mailbox.MailboxManager;
import com.ebiggz.postalservice.mailbox.MailboxManager.MailboxSelect;
import com.ebiggz.postalservice.permissions.PermissionHandler;
import com.ebiggz.postalservice.permissions.PermissionHandler.Perm;
import com.ebiggz.postalservice.screens.MainMenuGUI;

public class MailboxListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(final BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.CHEST) {
			Mailbox mb = MailboxManager.getInstance().getMailbox(event.getBlock().getLocation());
			if(mb != null) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_BREAK.toPrefixedString().replace("%owner%", mb.getOwner().getPlayerName()));
			}
		}
	}

	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent e) {
		if(!(e.getPlayer() instanceof Player)) return;
		Player player = (Player) e.getPlayer();
		if(GUIManager.getInstance().playerHasGUIOpen(player)) return;
		if (e.getInventory().getHolder() instanceof Chest) {
			Chest c = (Chest) e.getInventory().getHolder();
			Mailbox mailbox = MailboxManager.getInstance().getMailbox(c.getLocation());
			if(mailbox != null) {
				e.setCancelled(true);
				if(!PermissionHandler.playerHasPermission(Perm.MAIL_READ, player, true)) return;
				Bukkit.getServer().getPluginManager().callEvent(new PlayerOpenMailMenuEvent(player, UserFactory.getUser(player), mailbox));
				GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(player)), player);
			}
		}
	}


	@EventHandler
	public void onChestPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		if(item.getType() == Material.CHEST) {
			for(BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
				Block block = e.getBlock().getRelative(face);
				if(block.getType() == Material.CHEST) {
					if(MailboxManager.getInstance().locationHasMailbox(block.getLocation())) {
						e.getPlayer().sendMessage(Phrases.ERROR_CHEST_PLACE.toPrefixedString());
						e.setCancelled(true);
						break;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!event.hasBlock())
			return;
		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(MailboxManager.getInstance().mailboxSelectors.containsKey(event.getPlayer())) {
				if(event.getClickedBlock().getType() != Material.CHEST) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_NOT_CHEST.toPrefixedString());
				} else {
					MailboxSelect selectType = MailboxManager.getInstance().mailboxSelectors.get(event.getPlayer());
					MailboxManager.getInstance().mailboxSelectors.remove(event.getPlayer());
					event.setCancelled(true);
					if(selectType == MailboxSelect.SET) {
						try {
							MailboxManager.getInstance().addMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
							event.getPlayer().sendMessage(Phrases.ALERT_MAILBOX_REG.toPrefixedString());
						} catch (MailboxException me) {
							switch(me.getReason()) {
							case ALREADY_EXISTS:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_ALREADY_EXISTS.toPrefixedString());
								break;
							case NO_PERMISSION:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_NO_PERM.toPrefixedString());
								break;
							case DOUBLE_CHEST:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_DOUBLE_CHEST.toPrefixedString());
								break;
							case MAX_REACHED:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_MAX_REACHED.toPrefixedString());
								break;
							case CHEST_NOT_EMPTY:
								event.getPlayer().sendMessage("Mailbox chest must be empty!");
								break;
							default:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_UNKNOWN.toPrefixedString());
								break;
							}
						}
					} else {
						try {
							MailboxManager.getInstance().removeMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
							event.getPlayer().sendMessage(Phrases.ALERT_MAILBOX_UNREG.toPrefixedString());
						} catch (MailboxException me) {
							switch(me.getReason()) {
							case DOESNT_EXIST:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_DOESNT_EXIST.toPrefixedString());
								break;
							case NOT_OWNER:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_NOT_OWNER.toPrefixedString());
								break;
							default:
								event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_UNKNOWN.toPrefixedString());
								break;
							}
						}
					}
				}
			}
		}
	}
}