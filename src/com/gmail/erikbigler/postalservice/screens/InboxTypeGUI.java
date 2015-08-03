package com.gmail.erikbigler.postalservice.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.utils.Utils;

import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("all")
public class InboxTypeGUI implements GUI {

	private BoxType boxType;
	private User accountOwner;
	private int pageNumber;
	private int totalPages;
	private List<Mail> mails;

	public InboxTypeGUI(User accountOwner, BoxType type, int pageNumber) {
		this.boxType = type;
		this.pageNumber = pageNumber;
		this.accountOwner = accountOwner;
		this.mails = accountOwner.getBoxFromType(type);
	}

	public BoxType getType() {
		return boxType;
	}

	@Override
	public Inventory createInventory(Player player) {
		String boxTypeStr = boxType == BoxType.INBOX ? Phrases.BUTTON_INBOX.toString() : Phrases.BUTTON_SENT.toString();
		Inventory inventory = Bukkit.createInventory(null, 9*5, ChatColor.stripColor(boxTypeStr));
		int boxSize = mails.size();
		totalPages = (int) Math.ceil(boxSize/27.0);
		if(totalPages == 0) {
			totalPages = 1;
		}
		if(pageNumber > totalPages) {
			pageNumber = totalPages;
		}

		int mailIndex = 0 + (27*(pageNumber-1));
		int invIndex = 0;
		while(mailIndex < (27 * pageNumber) && mailIndex < boxSize) {
			inventory.setItem(invIndex,createMailButton(mails.get(mailIndex), player));
			mailIndex++;
			invIndex++;
		}

		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		ItemStack mainMenu = GUIUtils.createButton(
				Material.BOOK_AND_QUILL,
				Phrases.BUTTON_MAINMENU.toString(),
				Arrays.asList(
						Phrases.CLICK_ACTION_LEFTRETURN.toString()));
		inventory.setItem(40, mainMenu);
		totalPages = (int) Math.ceil(boxSize/27.0);
		if(totalPages > 1) {
			if(pageNumber + 1 <= totalPages) {
				ItemStack next = GUIUtils.createButton(
						Material.IRON_PLATE,
						ChatColor.GOLD + Phrases.BUTTON_NEXT.toString(),
						Arrays.asList(
								Phrases.CLICK_ACTION_NEXTPAGE.toString()));
				inventory.setItem(41, next);
				//next button
			}
			if(pageNumber - 1 >= 1) {
				ItemStack previous = GUIUtils.createButton(
						Material.IRON_PLATE,
						Phrases.BUTTON_PREVIOUS.toString(),
						Arrays.asList(
								Phrases.CLICK_ACTION_PREVIOUSPAGE.toString()));
				inventory.setItem(39, previous);
				//previous button
			}
		}
		GUIManager.getInstance().setGUIInv(this, Arrays.asList(inventory.getContents()));
		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {

		ItemStack clickedItem = clickedEvent.getCurrentItem();
		int clickedSlot = clickedEvent.getSlot();
		switch(clickedSlot) {
		case 39:
			if(clickedItem != null && clickedItem.getType() != Material.AIR) {
				this.pageNumber = pageNumber-1;
				clickedEvent.getInventory().setContents(createInventory(whoClicked).getContents());
			}
			break;
		case 40:
			if(clickedItem != null && clickedItem.getType() != Material.AIR) {
				GUIManager.getInstance().showGUI(new MainMenuGUI(accountOwner), whoClicked);
			}
			break;
		case 41:
			if(clickedItem != null && clickedItem.getType() != Material.AIR) {
				this.pageNumber = pageNumber+1;
				clickedEvent.getInventory().setContents(createInventory(whoClicked).getContents());
			}
			break;
		default:
			if(clickedSlot < 27 && clickedItem != null && clickedItem.getType() != Material.AIR) {
				if(!accountOwner.getPlayerName().equals(whoClicked.getName())) break;
				int mailIndex = clickedSlot + (27*(pageNumber-1));
				if(mails.size() < mailIndex+1) {
					whoClicked.closeInventory();
					whoClicked.sendMessage(Phrases.ERROR_INBOX.toPrefixedString());
					PostalService.getPlugin().getLogger().warning(whoClicked.getName() + " clicked on a " + clickedItem.getType().toString() + " which index (" + mailIndex + ") doesnt exist in their " + boxType.toString() + " mail array!");
					return;
				}
				Mail mail = mails.get(mailIndex);
				if(clickedEvent.getClick() == ClickType.LEFT) {
					if(boxType == BoxType.INBOX) {
						whoClicked.closeInventory();
						whoClicked.sendMessage(this.getReplySummaryString(mail));
						Utils.getComposeMessage(true, whoClicked).sendTo(whoClicked);
					}
				}
				else if(clickedEvent.getClick() == ClickType.RIGHT) {
					if(boxType == BoxType.INBOX) {
						if(mail.hasAttachments() && !mail.isClaimed()) {
							if(mail.getWorldGroup() == Config.getWorldGroupFromWorld(whoClicked.getWorld()) || mail.getWorldGroup().getName().equalsIgnoreCase("none") || Config.getMailTypesThatIgnoreWorldGroups().contains(mail.getType().getDisplayName())) {
								if(mail.getType().useSummaryScreen()) {
									GUIManager.getInstance().showGUI(new SummaryScreenGUI(mail, boxType, pageNumber), whoClicked);
								} else {
									try {
										mail.getType().administerAttachments(whoClicked);
										accountOwner.markMailAsClaimed(mail);
										whoClicked.sendMessage(Phrases.PREFIX + " " + mail.getType().getAttachmentClaimMessage());
										whoClicked.closeInventory();
									} catch (MailException e) {
										whoClicked.closeInventory();
										whoClicked.sendMessage(Phrases.PREFIX.toString() + " " + e.getMessage());
									}
								}
							}
						}
					}
				}
				else if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
					if(boxType == BoxType.SENT || !mail.hasAttachments() || mail.isClaimed()) {
						accountOwner.markMailAsDeleted(mail, boxType);
						mails.remove(mail);
						clickedEvent.getInventory().setContents(createInventory(whoClicked).getContents());
					}
				}
			}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {}


	private String getReplySummaryString(Mail mail) {
		if(!mail.getMessage().isEmpty()) {
			return Phrases.REPLY_SUMMARY_MESSAGE.toPrefixedString().replace("%sender%", mail.getSender()).replace("%mailtype%", mail.getType().getDisplayName()).replace("%timestamp%", mail.getTimeString(accountOwner.getTimeZone())).replace("%message%", mail.getMessage());
		} else {
			return Phrases.REPLY_SUMMARY_NOMESSAGE.toPrefixedString().replace("%sender%", mail.getSender()).replace("%mailtype%", mail.getType().getDisplayName()).replace("%timestamp%", mail.getTimeString(accountOwner.getTimeZone()));
		}
	}

	private ItemStack createMailButton(Mail mail, Player viewingPlayer) {

		ItemStack button = new ItemStack(mail.getType().getIcon());
		List<String> lore = new ArrayList<String>();

		String info = "";

		if(mail.hasAttachments()) {
			if(mail.isClaimed()) {
				info = ChatColor.GRAY + "*" + ChatColor.stripColor(Phrases.CLAIMED.toString()) + "*";
			} else {
				if(mail.getType().getAttachmentDescription() != null && !mail.getType().getAttachmentDescription().isEmpty()) {
					info = ChatColor.WHITE + ChatColor.stripColor(mail.getType().getAttachmentDescription());
				}
			}
		}
		if(!info.isEmpty()) {
			lore.add(info);
		}

		ItemMeta im = button.getItemMeta();
		String name = ChatColor.GOLD + "" + ChatColor.BOLD + Utils.capitalize(mail.getType().getDisplayName().toLowerCase(), null);
		im.setDisplayName(name);

		lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		if(!mail.getMessage().trim().isEmpty()) {
			String[] wrappedMessage = Utils.wrap("" +mail.getMessage() +"", 30, "\n", true).split("\n");
			for(String line : wrappedMessage) {
				lore.add(ChatColor.YELLOW + line);
			}
		}
		lore.add(boxType.equals(BoxType.INBOX) ? ChatColor.GRAY + "  " + Phrases.MAIL_ICON_FROM.toString() + " " + ChatColor.WHITE + mail.getSender() : ChatColor.GRAY + "  " + Phrases.MAIL_ICON_TO.toString() + " " + ChatColor.WHITE + mail.getRecipient());
		lore.add("  " + ChatColor.GRAY + mail.getTimeString(accountOwner.getTimeZone()));
		lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		if(accountOwner.getPlayerName().equals(viewingPlayer.getName())) {
			if(boxType.equals(BoxType.INBOX)) {
				lore.add(Phrases.CLICK_ACTION_RESPOND.toString());
				if(mail.hasAttachments()) {
					if(!mail.isClaimed()) {
						if(mail.getWorldGroup() == Config.getWorldGroupFromWorld(viewingPlayer.getWorld()) || mail.getWorldGroup().getName().equalsIgnoreCase("none") || Config.getMailTypesThatIgnoreWorldGroups().contains(mail.getType().getDisplayName())) {
							lore.add(Phrases.CLICK_ACTION_RIGHTCLAIM.toString());
						} else {
							lore.add(Phrases.MAIL_ICON_CLAIMWORLDGROUP.toString().replace("%worldgroup%", mail.getWorldGroup().getName()));
						}
					}
				}
			}
			if(boxType == BoxType.SENT || !mail.hasAttachments() || mail.isClaimed()) {
				lore.add(Phrases.CLICK_ACTION_DELETE.toString());
			}
		}

		im.setLore(lore);
		button.setItemMeta(im);
		return button;
	}

	@Override
	public boolean ignoreForeignItems() {
		return false;
	}
}
