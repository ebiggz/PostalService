package com.ebiggz.postalservice.screens;

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

import com.ebiggz.postalservice.apis.guiAPI.GUI;
import com.ebiggz.postalservice.apis.guiAPI.GUIManager;
import com.ebiggz.postalservice.apis.guiAPI.GUIUtils;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.Mail;
import com.ebiggz.postalservice.mail.MailManager.BoxType;

public class SummaryScreenGUI implements GUI {

	private Mail mail;
	private BoxType previous;
	private int prevPage;
	private ItemStack[] contents;
	private boolean atPostOffice;

	public SummaryScreenGUI(Mail mail, BoxType previous, int prevPage, boolean atPostOffice) {
		this.mail = mail;
		this.previous = previous;
		this.prevPage = prevPage;
		this.atPostOffice = atPostOffice;
	}

	@Override
	public ItemStack[] loadContents(Player player) {
		return contents;
	}

	@Override
	public Inventory createBaseInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9*5, mail.getType().getSummaryScreenTitle());

		for(ItemStack item : mail.getType().getSummaryIcons()) {
			inventory.addItem(item);
		}
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		if(previous == BoxType.INBOX) {
			if(mail.getType().onlyClaimableAtPostOffice() && !atPostOffice) {
				ItemStack claim = GUIUtils.createButton(
						mail.getType().getIcon(),
						mail.getType().getSummaryClaimButtonTitle(),
						Arrays.asList(
								ChatColor.GRAY + "*Visit a Post Office to Claim*",
								Phrases.CLICK_ACTION_RIGHTRETURN.toString()));
				inventory.setItem(40, claim);
			} else {
				ItemStack claim = GUIUtils.createButton(
						mail.getType().getIcon(),
						mail.getType().getSummaryClaimButtonTitle(),
						Arrays.asList(
								Phrases.CLICK_ACTION_LEFTCLAIM.toString(),
								Phrases.CLICK_ACTION_RIGHTRETURN.toString()));
				inventory.setItem(40, claim);
			}
		} else {
			ItemStack mainMenu = GUIUtils.createButton(
					Material.CHEST,
					Phrases.BUTTON_MAINMENU.toString(),
					Arrays.asList(
							Phrases.CLICK_ACTION_LEFTRETURN.toString()));
			inventory.setItem(40, mainMenu);
		}
		this.contents = inventory.getContents();
		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		if(clickedEvent.getSlot() == 40) {
			if(clickedEvent.getClick() == ClickType.LEFT) {
				try {
					if(mail.getType().onlyClaimableAtPostOffice() && !atPostOffice) return;
					mail.getType().administerAttachments(whoClicked);
					UserFactory.getUser(whoClicked.getName()).markMailAsClaimed(mail);
					whoClicked.sendMessage(Phrases.PREFIX.toString() + " " + mail.getType().getAttachmentClaimMessage());
					GUIManager.getInstance().showGUI(new InboxTypeGUI(UserFactory.getUser(whoClicked.getName()), previous, prevPage, atPostOffice), whoClicked);
				} catch (MailException e) {
					whoClicked.sendMessage(Phrases.PREFIX + " " + e.getErrorMessage());
					whoClicked.closeInventory();
				}
			} else {
				GUIManager.getInstance().showGUI(new InboxTypeGUI(UserFactory.getUser(whoClicked.getName()), previous, prevPage, atPostOffice), whoClicked);
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
