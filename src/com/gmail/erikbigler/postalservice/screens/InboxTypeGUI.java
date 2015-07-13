package com.gmail.erikbigler.postalservice.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.Mail.MailStatus;
import com.gmail.erikbigler.postalservice.mail.MailManager.InboxType;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.mail.mailtypes.Experience;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class InboxTypeGUI implements GUI {

	private InboxType boxType;
	private User user;
	private int pageNumber;
	private int totalPages;
	private Mail[] mails;

	public InboxTypeGUI(User user, InboxType type, int pageNumber) {
		this.boxType = type;
		this.pageNumber = pageNumber;
		this.user = user;
		this.mails = user.getBoxFromType(type);
	}

	public InboxType getType() {
		return boxType;
	}

	@Override
	public Inventory createInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9*5, player.getName() + "'s " + Utils.capitalize(boxType.toString().toLowerCase(), null));
		int boxSize = mails.length;
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
			inventory.setItem(invIndex,createMailButton(mails[mailIndex]));
			mailIndex++;
			invIndex++;
		}

		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		ItemStack mainMenu = GUIUtils.createButton(
				Material.BOOK_AND_QUILL,
				ChatColor.YELLOW +""+ ChatColor.BOLD + "Main Menu",
				Arrays.asList(
						ChatColor.RED + "Left-Click to " + ChatColor.BOLD + "Return"));
		inventory.setItem(40, mainMenu);
		totalPages = (int) Math.ceil(boxSize/27.0);
		if(totalPages > 1) {
			if(pageNumber + 1 <= totalPages) {
				ItemStack next = GUIUtils.createButton(
						Material.IRON_PLATE,
						ChatColor.GOLD + "Next " + ChatColor.STRIKETHROUGH + "->",
						Arrays.asList(
								ChatColor.RED + "Click for" + ChatColor.BOLD + " Next Page"));
				inventory.setItem(41, next);
				//next button
			}
			if(pageNumber - 1 >= 1) {
				ItemStack previous = GUIUtils.createButton(
						Material.IRON_PLATE,
						ChatColor.GOLD +""+ ChatColor.STRIKETHROUGH + "<- " + ChatColor.GOLD + "Previous",
						Arrays.asList(
								ChatColor.RED + "Click for" + ChatColor.BOLD + " Previous Page"));
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
					GUIManager.getInstance().showGUI(new InboxTypeGUI(user, boxType, pageNumber-1), whoClicked);
				}
				break;
			case 40:
				if(clickedItem != null && clickedItem.getType() != Material.AIR) {
					GUIManager.getInstance().showGUI(new MainMenuGUI(), whoClicked);
				}
				break;
			case 41:
				if(clickedItem != null && clickedItem.getType() != Material.AIR) {
					GUIManager.getInstance().showGUI(new InboxTypeGUI(user, boxType, pageNumber+1), whoClicked);
				}
				break;
			default:
				if(clickedSlot < 27 && clickedItem != null && clickedItem.getType() != Material.AIR) {
					int mailIndex = clickedSlot + (27*(pageNumber-1));
					if(mails.length < mailIndex+1) {
						whoClicked.closeInventory();
						whoClicked.sendMessage(ChatColor.RED + "[MPS] Something went wrong! Please try opening mail again. If the issue persists, make an issue on the website. Sorry!");
						Bukkit.getLogger().warning("[Mythica] " + whoClicked.getName() + " clicked on a " + clickedItem.getType().toString() + " which index (" + mailIndex + ") doesnt exist in their " + boxType.toString() + " mail array!");
						return;
					}
					Mail mail = mails[mailIndex];
					if(clickedEvent.getClick() == ClickType.LEFT) {
						if(boxType == InboxType.INBOX) {
							if(!mail.getSender().equals("the Trade Master")) {
								whoClicked.closeInventory();
								whoClicked.sendMessage(this.getReplySummaryString(mail));
								String command = "tellraw {player} {\"text\":\"\",\"extra\":[{\"text\":\"[MPS] Reply with a (Click one): \",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"\"}},{\"text\":\"Letter, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail letter to:{to} message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a text-only letter!\",\"color\":\"gold\"}]}}},{\"text\":\"Package, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail package to:{to} message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a package with the items in your drop box!\",\"color\":\"gold\"}]}}},{\"text\":\"Payment, \",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail payment to:{to} amount: message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail a money payment!\",\"color\":\"gold\"}]}}},{\"text\":\"Experience\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mail xp to:{to} amount: message:\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Mail xp points (not levels) to a player!!\",\"color\":\"gold\"}]}}}]}";
								Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("{player}", whoClicked.getName()).replace("{to}", mail.getFrom()));
							}
						}
					}
					else if(clickedEvent.getClick() == ClickType.RIGHT) {
						if(boxType == InboxType.INBOX) {
							if(mail.getStatus() != MailStatus.CLAIMED && mail.hasAttachments()) {
								mail.getType().administerAttachments(whoClicked);

								if(mail.getType() == MailType.PAYMENT) {
									Payment payment = (Payment) mail;
									Mythsentials.economy.depositPlayer(whoClicked.getName(), payment.getPayment());
									payment.setStatus(MailStatus.CLAIMED);
									mailbox.updateMailItem(payment, boxType);
									whoClicked.closeInventory();
									whoClicked.sendMessage(ChatColor.YELLOW +"[MPS] " + ChatColor.DARK_AQUA + "You have successfuly claimed the payment!");
								}
								else if(mail.getType() == MailType.PACKAGE) {
									GUIManager.getInstance().showGUI(new PackageGUI((PackageObj) mail, boxType, pageNumber), whoClicked);
								}
								else if(mail.getType() == MailType.TRADEGOODS) {
									GUIManager.getInstance().showGUI(new TradeGoodsGUI((TradeGoods) mail, boxType, pageNumber), whoClicked);
								}
							}
						}
					}
					else if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
						if((mail.getType() == MailType.TRADEGOODS || mail.getType() == MailType.PACKAGE || mail.getType() == MailType.PAYMENT || mail.getType() == MailType.EXPERIENCE) && mail.getStatus() != MailStatus.CLAIMED && boxType == MailboxType.INBOX) return;
						mailbox.deleteMailItem(mail, boxType);
						clickedEvent.getInventory().setContents(createInventory(whoClicked).getContents());
					}
				}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {}

	private String getReplySummaryString(Mail mail) {
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
	}

	private ItemStack createMailButton(Mail mail) {

		ItemStack button = new ItemStack(mail.getType().getIcon());
		List<String> lore = new ArrayList<String>();

		boolean claimed = (mail.getStatus() == MailStatus.CLAIMED);
		String info = "";
		switch(mail.getType()) {
			case LETTER:
				button = new ItemStack(Material.PAPER);
				break;
			case EXPERIENCE:
				button = new ItemStack(Material.EXP_BOTTLE);
				Experience exp = (Experience) mail;
				info = ChatColor.WHITE + "" + exp.getExperience() + " XP Point(s)";
				break;
			case PACKAGE:
				PackageObj po = (PackageObj) mail;
				info = ChatColor.WHITE + "" + po.getItems().size() + " Required Slot(s)";
				button = new ItemStack(Material.CHEST);
				break;
			case PAYMENT:
				Payment payment = (Payment) mail;
				info = ChatColor.WHITE + "$" + payment.getPayment();
				button = new ItemStack(Material.GOLD_INGOT);
				break;
			case TRADEGOODS:
				TradeGoods tradeGoods = (TradeGoods) mail;
				button = new ItemStack(Material.STORAGE_MINECART);
				if(tradeGoods.hasItems()) {
					info += ChatColor.WHITE + "" + tradeGoods.getItems().size() + " slot(s)";
				}
				if(tradeGoods.hasMoney()) {
					if(tradeGoods.hasItems()) {
						info += ChatColor.WHITE + ", ";
					}
					info += ChatColor.WHITE + "$" + tradeGoods.getMoney();
				}
				if(tradeGoods.hasXP()) {
					if(tradeGoods.hasItems() || tradeGoods.hasMoney()) {
						info += ChatColor.WHITE + ", ";
					}
					info += ChatColor.WHITE + "" + tradeGoods.getXP() + " XP point(s)";
				}

		}
		if(claimed) {
			info = ChatColor.GRAY + "*Claimed*";
		}
		if(!info.isEmpty()) {
			lore.add(info);
		}

		ItemMeta im = button.getItemMeta();
		String name = ChatColor.GOLD + "" + ChatColor.BOLD + Utils.capitalize(mail.getType().toString().toLowerCase(), null);
		if(mail.getType() == MailType.TRADEGOODS) {
			name = ChatColor.GOLD + "" + ChatColor.BOLD + "Trade Goods";
		}
		im.setDisplayName(name);

		lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		if(!mail.getMessage().trim().isEmpty()) {
			String[] wrappedMessage = Utils.wrap("" +mail.getMessage() +"", 30, "\n", true).split("\n");
			for(String line : wrappedMessage) {
				lore.add(ChatColor.YELLOW + line);
			}
		}
		lore.add(boxType.equals(MailboxType.INBOX) ? ChatColor.GRAY +""+ "  from " + ChatColor.WHITE + mail.getFrom() : ChatColor.GRAY + "  to " + ChatColor.WHITE + mail.getTo());
		lore.add("  " + ChatColor.GRAY + mail.getTimeStamp());
		lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		if(boxType.equals(MailboxType.INBOX)) {
			if(!mail.getFrom().equals("the Trade Master")) {
				lore.add(ChatColor.RED + "Left-Click to " + ChatColor.BOLD + "Respond");
			}
			if(mail.getType() != MailType.LETTER) {
				if(mail.getStatus() != MailStatus.CLAIMED) {
					lore.add(ChatColor.DARK_RED + "Right-Click to " + ChatColor.BOLD + "Claim");
				}
			}

		}
		if(mail.getType() == MailType.LETTER || mail.getStatus() == MailStatus.CLAIMED || boxType == MailboxType.SENT) {
			lore.add(ChatColor.RED + "Shift+Right-Click to " + ChatColor.BOLD + "Delete");
		}

		im.setLore(lore);
		button.setItemMeta(im);
		return button;

	}

	@Override
	public boolean ignoreForeignItems() {
		// TODO Auto-generated method stub
		return false;
	}
}
