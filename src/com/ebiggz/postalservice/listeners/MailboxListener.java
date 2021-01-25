package com.ebiggz.postalservice.listeners;

import java.util.Arrays;

import com.ebiggz.postalservice.mailbox.MailboxSelection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
				if(!mailbox.getOwner().getPlayerName().equals(player.getName())) {
					if(PermissionHandler.playerHasPermission(Perm.MAIL_READOTHER, player, false)) {
						GUIManager.getInstance().showGUI(
							new MainMenuGUI(UserFactory.getUser(mailbox.getOwner().getPlayerName()),
							mailbox.isPostOffice()),
							player
						);
					} else {
						player.sendMessage(ChatColor.RED + "You can't open a mailbox that isn't yours.");
					}
					return;
				}

				Bukkit.getServer().getPluginManager().callEvent(new PlayerOpenMailMenuEvent(player, UserFactory.getUser(player), mailbox));
				GUIManager.getInstance().showGUI(new MainMenuGUI(UserFactory.getUser(player), mailbox.isPostOffice()), player);
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

	private String getSetErrorMessage(MailboxException.Reason reason) {
		switch(reason) {
			case ALREADY_EXISTS:
				return Phrases.ERROR_MAILBOX_ALREADY_EXISTS.toPrefixedString();
			case NO_PERMISSION:
				return Phrases.ERROR_MAILBOX_NO_PERM.toPrefixedString();
			case DOUBLE_CHEST:
				return Phrases.ERROR_MAILBOX_DOUBLE_CHEST.toPrefixedString();
			case MAX_REACHED:
				return Phrases.ERROR_MAILBOX_MAX_REACHED.toPrefixedString();
			case CHEST_NOT_EMPTY:
				return "Mailbox chest must be empty!";
			default:
				return Phrases.ERROR_MAILBOX_UNKNOWN.toPrefixedString();
		}
	}

	private String getRemoveErrorMessage(MailboxException.Reason reason) {
		switch(reason) {
			case DOESNT_EXIST:
				return Phrases.ERROR_MAILBOX_DOESNT_EXIST.toPrefixedString();
			case NOT_OWNER:
				return Phrases.ERROR_MAILBOX_NOT_OWNER.toPrefixedString();
			default:
				return Phrases.ERROR_MAILBOX_UNKNOWN.toPrefixedString();
		}
	}

	private String getPostOfficeErrorMessage(MailboxException.Reason reason) {
		switch(reason) {
			case DOESNT_EXIST:
				return Phrases.ERROR_MAILBOX_DOESNT_EXIST.toPrefixedString();
			case NO_PERMISSION:
				return Phrases.ERROR_MAILBOX_NO_PERM.toPrefixedString();
			default:
				return Phrases.ERROR_MAILBOX_UNKNOWN.toPrefixedString();
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
					MailboxSelection selectionData = MailboxManager.getInstance().mailboxSelectors.get(event.getPlayer());
					MailboxManager.getInstance().mailboxSelectors.remove(event.getPlayer());
					event.setCancelled(true);
					switch(selectionData.getSelectionType()) {
						case SET:
							try {
								MailboxManager.getInstance().addMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
								event.getPlayer().sendMessage(Phrases.ALERT_MAILBOX_REG.toPrefixedString());
							} catch (MailboxException me) {
								event.getPlayer().sendMessage(getSetErrorMessage(me.getReason()));
							}
							break;
						case REMOVE:
							try {
								MailboxManager.getInstance().removeMailboxAtLoc(event.getClickedBlock().getLocation(), event.getPlayer());
								event.getPlayer().sendMessage(Phrases.ALERT_MAILBOX_UNREG.toPrefixedString());
							} catch (MailboxException me) {
								event.getPlayer().sendMessage(getRemoveErrorMessage(me.getReason()));
							}
							break;
						case SET_OTHER:
							try {
								MailboxManager.getInstance().addMailboxAtLocForOther(event.getPlayer(), selectionData.getOwnerName(), event.getClickedBlock().getLocation());
								event.getPlayer().sendMessage(ChatColor.AQUA + "Added mailbox for " + selectionData.getOwnerName());
							} catch (MailboxException me) {
								event.getPlayer().sendMessage(getSetErrorMessage(me.getReason()));
							}
							break;
						case MARK_POST_OFFICE:
							try {
								boolean isPostOffice = MailboxManager.getInstance().toggleMailboxPostOfficeStatus(event.getPlayer(), event.getClickedBlock().getLocation());
								event.getPlayer().sendMessage(ChatColor.AQUA + "Set mailbox post office status to: " + ChatColor.GOLD + (isPostOffice ? "true" : "false"));
							} catch (MailboxException me) {
								event.getPlayer().sendMessage(getPostOfficeErrorMessage(me.getReason()));
							}
							break;
						default:
							event.getPlayer().sendMessage("Unknown mailbox selection action!");
					}
				}
			}
		}
	}
}