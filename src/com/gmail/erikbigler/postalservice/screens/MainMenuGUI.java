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
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;

public class MainMenuGUI implements GUI {

	@Override
	public Inventory createInventory(Player player) {

		User user = UserFactory.getUser(player.getUniqueId());
		int unread = user.getUnreadMailCount(Config.getWorldGroupFromWorld(player.getWorld().getName()));
		/*int inboxSize = user.getInbox().length;
		int sentboxSize = user.getSentbox().length;*/

		Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, Phrases.MAINMENU_TITLE.toString());

		ItemStack infoSign = GUIUtils.createButton(Material.SIGN, Phrases.BUTTON_ACCOUNTINFO.toString(), Arrays.asList(ChatColor.GRAY + "Mailboxes: " + ChatColor.WHITE + /*MailboxManager.getInstance().getMailboxCount(user.getPlayerName()) +*/ " of " /*+ user.getMaxMailboxCount()*/, ChatColor.GRAY + "Inbox/Sent Size: " + ChatColor.WHITE, Phrases.CLICK_ACTION_HELP.toString()));

		ItemStack composeBook = GUIUtils.createButton(Material.BOOK_AND_QUILL, Phrases.BUTTON_COMPOSE.toString(), (Config.packagesAreEnabled() ? Arrays.asList(Phrases.CLICK_ACTION_COMPOSE.toString(), Phrases.CLICK_ACTION_DROPBOX.toString(), Phrases.DROPBOX_DESCRIPTION.toString()) : Arrays.asList(Phrases.CLICK_ACTION_COMPOSE.toString())));

		ItemStack inboxChest = GUIUtils.createButton(Material.CHEST, Phrases.BUTTON_INBOX.toString(), Arrays.asList(ChatColor.WHITE + "" + unread + " Unread", Phrases.CLICK_ACTION_OPEN.toString()));

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

		User user = UserFactory.getUser(whoClicked.getName());

		int slot = clickedEvent.getSlot();
		switch(slot) {
		case 0:
			if(clickedEvent.getClick() == ClickType.LEFT) {
				whoClicked.closeInventory();
				whoClicked.performCommand("mail help");
			}
			break;
		case 1:
			if(clickedEvent.getClick() == ClickType.LEFT) {
				if(clickedEvent.getCursor() != null) {
					if(clickedEvent.getCursor().getType() == Material.BOOK_AND_QUILL) {
						BookMeta bm = (BookMeta) clickedEvent.getCursor().getItemMeta();
						if(bm.hasPages()) {
							String pageData = bm.getPage(1).replaceAll(System.getProperty("line.separator"), "");
							String regex = "[tT][oO]:(\\s)?(\\w+)\\b|$";
							Matcher matcher = Pattern.compile(regex).matcher(pageData);
							if(matcher.find()) {
								String[] split = matcher.group().split(":");
								String to = split[1].trim();
								String message = pageData.replace(matcher.group(), "");
								whoClicked.performCommand(Phrases.COMMAND_MAIL.toString() + " " + Phrases.MAILTYPE_LETTER.toString() + " " + Phrases.COMMAND_ARG_TO + ":" + to + " " + Phrases.COMMAND_ARG_MESSAGE.toString() + ":" + message.trim());
								MailboxManager.getInstance().willDropBook.add(whoClicked);
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
			break;
		case 2:
			user.markAllMailAsRead(Config.getCurrentWorldGroupForUser(user));
			GUIManager.getInstance().showGUI(new InboxTypeGUI(user, BoxType.INBOX, 1), whoClicked);
			break;
		case 3:
			GUIManager.getInstance().showGUI(new InboxTypeGUI(user, BoxType.SENT, 1), whoClicked);
			break;
		case 4:
			if(whoClicked.isOp()) {
				//GUIManager.getInstance().showGUI(new TradingPostMenuGUI(), whoClicked);
			}
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
