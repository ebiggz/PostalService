package com.gmail.erikbigler.postalservice.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException;
import com.gmail.erikbigler.postalservice.mailbox.Mailbox;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager.MailboxSelect;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler.Perm;
import com.gmail.erikbigler.postalservice.screens.MainMenuGUI;

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

	//TODO: Convert GUI shower from interact to open event
	/*@EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e){
        if (e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof DoubleChest){
            // rawr
        }
    }*/

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!event.hasBlock())
			return;
		if(event.getClickedBlock().getType() != Material.CHEST) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(MailboxManager.getInstance().mailboxSelectors.containsKey(event.getPlayer())) {
					MailboxManager.getInstance().mailboxSelectors.remove(event.getPlayer());
					event.setCancelled(true);
					event.getPlayer().sendMessage(Phrases.ERROR_MAILBOX_NOT_CHEST.toPrefixedString());
				}
			}
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(MailboxManager.getInstance().locationHasMailbox(event.getClickedBlock().getLocation())) {
				event.setCancelled(true);
				if(!PermissionHandler.playerHasPermission(Perm.MAIL_READ, event.getPlayer(), true)) return;
				GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(event.getPlayer())), event.getPlayer());
			}
		} else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(MailboxManager.getInstance().mailboxSelectors.containsKey(event.getPlayer())) {
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