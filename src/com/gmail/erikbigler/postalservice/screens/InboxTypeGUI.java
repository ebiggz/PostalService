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
import com.gmail.erikbigler.postalservice.configs.Config;
import com.gmail.erikbigler.postalservice.configs.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.MailManager.BoxType;
import com.gmail.erikbigler.postalservice.utils.Utils;

import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("all")
public class InboxTypeGUI implements GUI {

	private BoxType boxType;
	private User user;
	private int pageNumber;
	private int totalPages;
	private List<Mail> mails;

	public InboxTypeGUI(User user, BoxType type, int pageNumber) {
		this.boxType = type;
		this.pageNumber = pageNumber;
		this.user = user;
		this.mails = user.getBoxFromType(type, Config.getWorldGroupFromWorld(Utils.getPlayerFromIdentifier(user.getIdentifier()).getWorld()));
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
			inventory.setItem(invIndex,createMailButton(mails.get(mailIndex)));
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
				GUIManager.getInstance().showGUI(new MainMenuGUI(), whoClicked);
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
						String command = "tellraw {player} {\"text\":\"\",\"extra\":[{\"text\":\"[MPS] Reply with a (Click one): \",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"\"}},{\"text\":\"Letter, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail letter to:{to} message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a text-only letter!\",\"color\":\"gold\"}]}}},{\"text\":\"Package, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail package to:{to} message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a package with the items in your drop box!\",\"color\":\"gold\"}]}}},{\"text\":\"Payment, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail payment to:{to} amount: message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a money payment!\",\"color\":\"gold\"}]}}},{\"text\":\"Experience\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail xp to:{to} amount: message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail xp points (not levels) to a player!!\",\"color\":\"gold\"}]}}}]}";
						//Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("{player}", whoClicked.getName()).replace("{to}", mail.getFrom()));
					}
				}
				else if(clickedEvent.getClick() == ClickType.RIGHT) {
					if(boxType == BoxType.INBOX) {
						if(mail.hasAttachments() && !mail.isClaimed()) {
							if(mail.getType().useSummaryScreen()) {
								//go to summary screen
							} else {
								try {
									mail.getType().administerAttachments(whoClicked);
									user.markMailAsClaimed(mail);
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
				else if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
					if(boxType == BoxType.SENT || !mail.hasAttachments() || mail.isClaimed()) {
						user.markMailAsDeleted(mail);
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
		/*
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.YELLOW + "[MPS] " + ChatColor.AQUA + mail.getFrom() + ChatColor.DARK_AQUA + " mailed you ");
		switch(mail.getType()) {
			case EXPERIENCE:
				Experience exp = (Experience) mail;
				sb.append("an " + ChatColor.AQUA + "Experience Sum" + ChatColor.DARK_AQUA + " of " + ChatColor.AQUA + exp.getExperience() + " xp point(s)" + ChatColor.DARK_AQUA);
				break;
			case LETTER:
				sb.append("a " + ChatColor.AQUA + "Letter" + ChatColor.DARK_AQUA);
				break;
			case PACKAGE:
				PackageObj po = (PackageObj) mail;
				sb.append("a " + ChatColor.AQUA + "Package" + ChatColor.DARK_AQUA + " containing " + ChatColor.AQUA + po.getItems().size() + " item(s)"  + ChatColor.DARK_AQUA);
				break;
			case PAYMENT:
				Payment payment = (Payment) mail;
				sb.append("a " + ChatColor.AQUA + "Payment" + ChatColor.DARK_AQUA + " of " + ChatColor.DARK_AQUA + "$" + payment.getPayment() + ChatColor.DARK_AQUA);
				break;
			case TRADEGOODS:
				break;
		}
		if(!mail.getMessage().isEmpty()) {
			sb.append(" with the message: ");
			sb.append(ChatColor.RESET + "\"" + ChatColor.ITALIC + mail.getMessage() + "\"");
		}
		sb.append(ChatColor.DARK_AQUA + " at " + ChatColor.AQUA + mail.getTimeStamp());
		return sb.toString();
		 */
		return null;
	}

	private ItemStack createMailButton(Mail mail) {

		ItemStack button = new ItemStack(mail.getType().getIcon());
		List<String> lore = new ArrayList<String>();

		String info = "";

		if(mail.hasAttachments()) {
			if(mail.isClaimed()) {
				info = ChatColor.GRAY + "*" + Phrases.CLAIMED.toString() + "*";
			} else {
				info = ChatColor.GRAY + ChatColor.stripColor(mail.getType().getAttachmentDescription());
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
		lore.add(boxType.equals(BoxType.INBOX) ? ChatColor.GRAY + "  " + Phrases.FROM.toString() + " " + ChatColor.WHITE + mail.getSender() : ChatColor.GRAY + "  " + Phrases.TO.toString() + " " + ChatColor.WHITE + mail.getRecipient());
		lore.add("  " + ChatColor.GRAY + mail.getTimeString());
		lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		if(boxType.equals(BoxType.INBOX)) {
			lore.add(Phrases.CLICK_ACTION_RESPOND.toString());
			if(mail.hasAttachments()) {
				if(!mail.isClaimed()) {
					lore.add(Phrases.CLICK_ACTION_RIGHTCLAIM.toString());
				}
			}
		}
		if(boxType == BoxType.SENT || !mail.hasAttachments() || mail.isClaimed()) {
			lore.add(Phrases.CLICK_ACTION_DELETE.toString());
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
