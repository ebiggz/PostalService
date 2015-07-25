package com.gmail.erikbigler.postalservice.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager.MailboxSelect;
import com.gmail.erikbigler.postalservice.screens.MainMenuGUI;

public class MailboxListener implements Listener {

	//TODO: Create phrases for mailboxlistner

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(final BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.CHEST) {
			if(MailboxManager.getInstance().locationHasMailbox(event.getBlock().getLocation())) {
				event.setCancelled(true);
				//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] This chest is registered as a mailbox. The owner must deregister the chest before it can be removed.");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!event.hasBlock())
			return;
		if(event.getClickedBlock().getType() != Material.CHEST) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(MailboxManager.getInstance().mailboxSelectors.containsKey(event.getPlayer())) {
					MailboxManager.getInstance().mailboxSelectors.remove(event.getPlayer());
					event.setCancelled(true);
					//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] Selected block is not a chest! Mailbox selection canceled.");
				}
			}
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(MailboxManager.getInstance().locationHasMailbox(event.getClickedBlock().getLocation())) {
				if(!event.getPlayer().isSneaking()) {
					event.setCancelled(true);
					GUIManager.getInstance().showGUI(new MainMenuGUI(), event.getPlayer());
				}
			}
		} else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(MailboxManager.getInstance().mailboxSelectors.containsKey(event.getPlayer())) {
				MailboxSelect selectType = MailboxManager.getInstance().mailboxSelectors.get(event.getPlayer());
				MailboxManager.getInstance().mailboxSelectors.remove(event.getPlayer());
				event.setCancelled(true);
				if(selectType == MailboxSelect.SET) {
					try {
						MailboxManager.getInstance().addMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "[MPS]" + ChatColor.AQUA + " Mailbox registered!");
					} catch (MailboxException me) {
						switch(me.getReason()) {
						case ALREADY_EXISTS:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] A mailbox already exsists here! Selection canceled.");
							break;
						case NO_PERMISSION:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] Unable to set mailbox, you don't have permission to build or place here! Selection canceled.");
							break;
						default:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] There was an error setting your mailbox. Please try again.");
							break;
						}
					}
				} else {
					try {
						MailboxManager.getInstance().removeMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "[MPS]" + ChatColor.AQUA + " Mailbox unregistered!");
					} catch (MailboxException me) {
						switch(me.getReason()) {
						case DOESNT_EXIST:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] A mailbox doesn't exsist here! Selection canceled.");
							break;
						case NOT_OWNER:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] Unable to remove mailbox, you don't own this mailbox! Selection canceled.");
							break;
						default:
							//event.getPlayer().sendMessage(ChatColor.RED + "[MPS] There was an error setting your mailbox. Please try again.");
							break;
						}
					}
				}
			}
		}
	}
}