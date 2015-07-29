package com.gmail.erikbigler.postalservice.screens;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler.Perm;

public class MainMenuGUI implements GUI {

	private User accountOwner;

	public MainMenuGUI(User accountOwner) {
		this.accountOwner = accountOwner;
	}

	@Override
	public Inventory createInventory(Player viewingPlayer) {

		int unread = accountOwner.getUnreadMailCount();
		int inboxSize = accountOwner.getBoxSizeFromType(BoxType.INBOX);

		Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, Phrases.MAINMENU_TITLE.toString());


		ItemStack infoSign = GUIUtils.createButton(
				Material.SIGN,
				Phrases.BUTTON_ACCOUNTINFO.toString(),
				Arrays.asList(
						ChatColor.GRAY + Phrases.ACCOUNT_INFO_MAILBOXES.toString() + ": " + ChatColor.WHITE + MailboxManager.getInstance().getMailboxCount(accountOwner.getPlayerName(), Config.getWorldGroupFromWorld(viewingPlayer.getWorld())) + "/" + Config.getMailboxLimitForPlayer(accountOwner.getPlayerName()),
						ChatColor.GRAY + Phrases.ACCOUNT_INFO_INBOXSIZE.toString() + ": " + ChatColor.WHITE + inboxSize + "/" + Config.getMaxInboxSizeForPlayer(accountOwner.getPlayerName()),
						Phrases.CLICK_ACTION_HELP.toString()));

		ItemStack composeBook = GUIUtils.createButton(
				Material.BOOK_AND_QUILL,
				Phrases.BUTTON_COMPOSE.toString(),
				accountOwner.getPlayerName().equals(viewingPlayer.getName())
				? (Config.packagesAreEnabled()
						? Arrays.asList(
								Phrases.CLICK_ACTION_COMPOSE.toString(),
								Phrases.CLICK_ACTION_DROPBOX.toString(),
								Phrases.DROPBOX_DESCRIPTION.toString())
								: Arrays.asList(Phrases.CLICK_ACTION_COMPOSE.toString()))
						:  Arrays.asList(""));

		ItemStack inboxChest = GUIUtils.createButton(Material.CHEST, Phrases.BUTTON_INBOX.toString(), Arrays.asList(ChatColor.WHITE + Phrases.BUTTON_INBOX_UNREAD.toString().replace("%count%", Integer.toString(unread)), Phrases.CLICK_ACTION_OPEN.toString()));

		ItemStack sentEnderchest = GUIUtils.createButton(Material.ENDER_CHEST, Phrases.BUTTON_SENT.toString(), Arrays.asList(Phrases.CLICK_ACTION_OPEN.toString()));

		ItemStack tradingPost = GUIUtils.createButton(Material.FENCE, Phrases.BUTTON_TRADINGPOST.toString(), Arrays.asList(ChatColor.GRAY + "*Coming Soon*"));

		inventory.setItem(0, infoSign);
		inventory.setItem(1, composeBook);
		inventory.setItem(2, inboxChest);
		inventory.setItem(3, sentEnderchest);
		inventory.setItem(4, tradingPost);

		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {

		if(!whoClicked.getName().equals(accountOwner.getPlayerName())) {
			if(!PermissionHandler.playerHasPermission(Perm.MAIL_READOTHER, whoClicked, true)) {
				whoClicked.closeInventory();
				return;
			}
		}

		int slot = clickedEvent.getSlot();
		switch(slot) {
		case 0:
			if(clickedEvent.getClick() == ClickType.LEFT) {
				whoClicked.closeInventory();
				whoClicked.performCommand(Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_HELP.toString());
			}
			break;
		case 1:
			if(accountOwner.getPlayerName().equals(whoClicked.getName())) {
				if(clickedEvent.getClick() == ClickType.LEFT) {
					if(clickedEvent.getCursor() != null) {
						if(clickedEvent.getCursor().getType() == Material.BOOK_AND_QUILL) {
							BookMeta bm = (BookMeta) clickedEvent.getCursor().getItemMeta();
							if(bm.hasPages()) {
								String pageData = bm.getPage(1).replaceAll(System.getProperty("line.separator"), "");
								String regex = Language.getToRegex();
								Matcher matcher = Pattern.compile(regex).matcher(pageData);
								if(matcher.find()) {
									String[] split = matcher.group().split(":");
									String to = split[1].trim();
									String message = pageData.replace(matcher.group(), "");
									whoClicked.performCommand(Phrases.COMMAND_MAIL.toString() + " " + Phrases.MAILTYPE_LETTER.toString() + " " + Phrases.COMMAND_ARG_TO + ":" + to + " " + Phrases.COMMAND_ARG_MESSAGE.toString() + ":" + message.trim());
									MailManager.getInstance().willDropBook.add(whoClicked);
									whoClicked.closeInventory();
									break;
								}
							}
						}
					}
					whoClicked.closeInventory();
					whoClicked.performCommand(Phrases.COMMAND_MAIL.toString() + " " + Phrases.COMMAND_ARG_COMPOSE.toString());
				} else if(clickedEvent.getClick() == ClickType.RIGHT) {
					if(Config.packagesAreEnabled())
						GUIManager.getInstance().showGUI(new DropboxGUI(), whoClicked);
				}
			}
			break;
		case 2:
			if(accountOwner.getPlayerName().equals(whoClicked.getName())) {
				accountOwner.markAllMailAsRead();
			}
			GUIManager.getInstance().showGUI(new InboxTypeGUI(accountOwner, BoxType.INBOX, 1), whoClicked);
			break;
		case 3:
			GUIManager.getInstance().showGUI(new InboxTypeGUI(accountOwner, BoxType.SENT, 1), whoClicked);
			break;
		case 4:
			// TODO: Go to the trading post gui
			break;
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {}

	@Override
	public boolean ignoreForeignItems() {
		return false;
	}

}
