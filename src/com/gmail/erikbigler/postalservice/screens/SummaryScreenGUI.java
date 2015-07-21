package com.gmail.erikbigler.postalservice.screens;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;

public class SummaryScreenGUI implements GUI {

	private Mail mail;
	private BoxType previous;
	private int prevPage;

	public SummaryScreenGUI(Mail mail, BoxType previous, int prevPage) {
		this.mail = mail;
		this.previous = previous;
		this.prevPage = prevPage;
	}

	@Override
	public Inventory createInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9*5, mail.getType().getSummaryScreenTitle());

		for(ItemStack item : mail.getType().getSummaryIcons()) {
			inventory.addItem(item);
		}
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		if(previous == BoxType.INBOX) {
			ItemStack claim = GUIUtils.createButton(
					mail.getType().getIcon(),
					mail.getType().getSummaryClaimButtonTitle(),
					Arrays.asList(
							Phrases.CLICK_ACTION_LEFTCLAIM.toString(),
							Phrases.CLICK_ACTION_RIGHTRETURN.toString()));
			inventory.setItem(40, claim);
		} else {
			ItemStack mainMenu = GUIUtils.createButton(
					Material.CHEST,
					Phrases.BUTTON_MAINMENU.toString(),
					Arrays.asList(
							Phrases.CLICK_ACTION_LEFTRETURN.toString()));
			inventory.setItem(40, mainMenu);
		}
		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		if(clickedEvent.getSlot() == 40) {
			if(clickedEvent.getClick() == ClickType.LEFT) {
				try {
					mail.getType().administerAttachments(whoClicked);
					UserFactory.getUser(whoClicked.getName()).markMailAsClaimed(mail);
					whoClicked.sendMessage(Phrases.PREFIX.toString() + " " + mail.getType().getAttachmentClaimMessage());
					whoClicked.closeInventory();
				} catch (MailException e) {
					whoClicked.sendMessage(Phrases.PREFIX + " " + e.getErrorMessage());
					whoClicked.closeInventory();
				}
			} else {
				GUIManager.getInstance().showGUI(new InboxTypeGUI(UserFactory.getUser(whoClicked.getName()), previous, prevPage), whoClicked);
			}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {
	}

	@Override
	public boolean ignoreForeignItems() {
		return false;
	}
}
