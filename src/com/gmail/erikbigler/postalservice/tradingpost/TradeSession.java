package com.gmail.erikbigler.postalservice.tradingpost;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.FormattedText;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessage;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.mail.Mail;
import com.gmail.erikbigler.postalservice.mail.Mail.MailStatus;
import com.gmail.erikbigler.postalservice.mailbox.MailboxManager;


public class TradeSession /*implements GUI*/ {

	/*private String initiatorName;
	private String inviteeName;

	private AcceptOfferButton initiatorAccept;
	private AcceptOfferButton inviteeAccept;
	private LockOfferButton initiatorLocked;
	private LockOfferButton inviteeLocked;
	private TradeMoneyXpButton initiatorMoneyXP;
	private TradeMoneyXpButton inviteeMoneyXP;
	private TradeLogButton tradeLog;
	private boolean isEnded = false;

	private int[] initiatorItemArea = {0,1,2,3,9,10,11,12,18,19,20,21};
	private int[] inviteeItemArea = {5,6,7,8,14,15,16,17,23,24,25,26};

	private Inventory inventory;

	public TradeSession(String initiatorName, String inviteeName) {
		this.initiatorName = initiatorName;
		this.inviteeName = inviteeName;
		tradeLog = new TradeLogButton();
		tradeLog.addLogEntry(initiatorName + " started the trade");
		this.initiatorAccept = new AcceptOfferButton(initiatorName, inviteeName);
		this.inviteeAccept = new AcceptOfferButton(inviteeName, initiatorName);
		this.initiatorLocked = new LockOfferButton(initiatorName);
		this.inviteeLocked = new LockOfferButton(inviteeName);
		this.initiatorMoneyXP = new TradeMoneyXpButton(initiatorName);
		this.inviteeMoneyXP = new TradeMoneyXpButton(inviteeName);
		this.inventory = this.generateInv();
	}

	private Inventory generateInv() {
		Inventory inventory = Bukkit.createInventory(null, 9*5, "Trading Session");
		//create button separators
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}
		int count = 4;
		while(count <= 40) {
			if(count != 4 || count != 31) {
				inventory.setItem(count, seperator);
			}
			count += 9;
		}


		//help info sign
		List<String> infoSignLore = new ArrayList<String>();
		infoSignLore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "-----------");
		//Utils.addArrayToList(Utils.wrap(ChatColor.WHITE + "* Each player has their own section to place items for trade.", 30, "\n"+ ChatColor.WHITE, true).split("\n"), infoSignLore);
		infoSignLore.add(" ");
		//Utils.addArrayToList(Utils.wrap(ChatColor.WHITE + "* Once a player has placed their offer they must press their \"Lock Offer\" button.", 30, "\n"+ ChatColor.WHITE, true).split("\n"), infoSignLore);
		//infoSignLore.add(" ");
		//Utils.addArrayToList(Utils.wrap(ChatColor.WHITE + "* After a player locks in their offer, the other player can then accept it.", 30, "\n"+ ChatColor.WHITE, true).split("\n"), infoSignLore);
		infoSignLore.add(" ");
		//Utils.addArrayToList(Utils.wrap(ChatColor.WHITE + "* Both players must accept each other's offers for the trade to be successful.", 30, "\n"+ ChatColor.WHITE, true).split("\n"), infoSignLore);

		ItemStack infoSign = GUIUtils.createButton(
				Material.SIGN,
				ChatColor.YELLOW +""+ChatColor.BOLD + "Help",
				infoSignLore);
		inventory.setItem(4, infoSign);


		inventory.setItem(31, tradeLog.toItemStack());
		inventory.setItem(36, initiatorMoneyXP.toItemStack());
		inventory.setItem(37, initiatorLocked.toItemStack());
		inventory.setItem(38, initiatorAccept.toItemStack());
		inventory.setItem(39, GUIUtils.createButton(
				Utils.getPlayerHeadItem(initiatorName),
				ChatColor.YELLOW +""+ ChatColor.BOLD +initiatorName + "'s Section",
				Arrays.asList(
						ChatColor.GRAY + "*" + initiatorName + ", shift + right click here",
						ChatColor.GRAY + "to cancel the trade session*")));

		inventory.setItem(41, GUIUtils.createButton(
				Utils.getPlayerHeadItem(inviteeName),
				ChatColor.YELLOW +""+ChatColor.BOLD + inviteeName + "'s Section",
				Arrays.asList(
						ChatColor.GRAY + "*" + inviteeName + ", shift + right click here",
						ChatColor.GRAY + "to cancel the trade session*")));
		inventory.setItem(42, inviteeAccept.toItemStack());
		inventory.setItem(43, inviteeLocked.toItemStack());
		inventory.setItem(44, inviteeMoneyXP.toItemStack());

		return inventory;
	}

	public Inventory createInventory(Player player) {
		return inventory;
	}

	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		int slot = clickedEvent.getSlot();
		if(whoClicked.getName().equals(initiatorName)) {
			if(this.indexIsInInitiatorItemArea(slot)) {
				if(!initiatorLocked.isLocked()) {
					clickedEvent.setCancelled(false);
					this.initiatorLocked.setTradeIsEmpty(this.initiatorHasEmptyTrade());
					this.updateTradeButton(TradeButton.INITIATOR_LOCKIN);
				}
			}
			else {
				switch(slot) {
					case 36: //initiator moneyxp button clicked
						this.showInteractiveMoneyXpMessage(whoClicked);
						break;
					case 37: //initiator lock in button clicked
						if(this.initiatorHasEmptyTrade()) return;
						initiatorLocked.shouldLock(!initiatorLocked.isLocked());
						this.updateTradeButton(TradeButton.INITIATOR_LOCKIN);
						if(initiatorLocked.isLocked()) {
							tradeLog.addLogEntry(initiatorName + " locked in their offer");
							if(inviteeAccept.isWaiting()) {
								inviteeAccept.setWaiting(false);
								this.updateTradeButton(TradeButton.INVITEE_ACCEPT);
							}
						} else {
							tradeLog.addLogEntry(initiatorName + " unlocked their offer");
							if(inviteeAccept.isAccepted()) {
								inviteeAccept.setAccept(false);
							}
							if(!inviteeAccept.isWaiting()) {
								inviteeAccept.setWaiting(true);
							}
							this.updateTradeButton(TradeButton.INVITEE_ACCEPT);
						}
						this.updateTradeButton(TradeButton.TRADE_LOG);
						break;
					case 38: //initiator accept button clicked
						if(inviteeLocked.isLocked()) {
							if(!initiatorAccept.isAccepted()) {
								initiatorAccept.setAccept(true);
								tradeLog.addLogEntry(initiatorName + " accepted " + inviteeName + "'s offer");
							} else {
								initiatorAccept.setAccept(false);
								tradeLog.addLogEntry(initiatorName + " unaccepted " + inviteeName + "'s offer");
							}
							this.updateTradeButton(TradeButton.TRADE_LOG);
							this.updateTradeButton(TradeButton.INITIATOR_ACCEPT);
							this.checkForSuccess();
						}
						break;
					case 39:
						if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
							this.cancelTrade();
						}
						break;
					default:
						break;
				}
			}
		}
		else if(whoClicked.getName().equals(inviteeName)) {
			if(this.indexIsInInviteeItemArea(slot)) {
				if(!inviteeLocked.isLocked()) {
					clickedEvent.setCancelled(false);
					this.inviteeLocked.setTradeIsEmpty(this.inviteeHasEmptyTrade());
					this.updateTradeButton(TradeButton.INVITEE_LOCKIN);
				}
			}
			else {
				switch(slot) {
					case 44: //invitee moneyxp button clicked
						this.showInteractiveMoneyXpMessage(whoClicked);
						break;
					case 43: //invitee lock in button clicked
						if(this.inviteeHasEmptyTrade()) return;
						inviteeLocked.shouldLock(!inviteeLocked.isLocked());
						this.updateTradeButton(TradeButton.INVITEE_LOCKIN);
						if(inviteeLocked.isLocked()) {
							tradeLog.addLogEntry(inviteeName + " locked in their offer");
							if(initiatorAccept.isWaiting()) {
								initiatorAccept.setWaiting(false);
								this.updateTradeButton(TradeButton.INITIATOR_ACCEPT);
							}
						} else {
							tradeLog.addLogEntry(inviteeName + " unlocked their offer");
							if(initiatorAccept.isAccepted()) {
								initiatorAccept.setAccept(false);
							}
							if(!initiatorAccept.isWaiting()){
								initiatorAccept.setWaiting(true);
							}
							this.updateTradeButton(TradeButton.INITIATOR_ACCEPT);
						}
						this.updateTradeButton(TradeButton.TRADE_LOG);
						break;
					case 42: //invitee accept button clicked
						if(initiatorLocked.isLocked()) {
							if(!inviteeAccept.isAccepted()) {
								inviteeAccept.setAccept(true);
								tradeLog.addLogEntry(inviteeName + " accepted " + initiatorName + "'s offer");
							} else {
								inviteeAccept.setAccept(false);
								tradeLog.addLogEntry(inviteeName + " unaccepted " + initiatorName + "'s offer");
							}
							this.updateTradeButton(TradeButton.TRADE_LOG);
							this.updateTradeButton(TradeButton.INVITEE_ACCEPT);
							this.checkForSuccess();
						}
						break;
					case 41:
						if(clickedEvent.getClick() == ClickType.SHIFT_RIGHT) {
							this.cancelTrade();
						}
						break;
					default:
						break;
				}
			}
		}
	}

	private void showInteractiveMoneyXpMessage(Player player) {
		InteractiveMessage im = new InteractiveMessage();
		im.addElement("[Tranding Post] ", ChatColor.YELLOW);
		im.addElement("Click one to add it in your current trade session: ", ChatColor.DARK_AQUA);
		im.addElement(new InteractiveMessageElement(
				new FormattedText("Money offer", ChatColor.AQUA),
				HoverEvent.SHOW_TEXT,
				new FormattedText(ChatColor.YELLOW + "Click to offer money in current trade.\n" +ChatColor.GRAY + "IE /trade money 30.50"),
				ClickEvent.SUGGEST_COMMAND,
				"/trade money "));
		im.addElement(" or ", ChatColor.DARK_AQUA);
		im.addElement(new InteractiveMessageElement(
				new FormattedText("XP offer", ChatColor.AQUA),
				HoverEvent.SHOW_TEXT,
				new FormattedText(ChatColor.YELLOW + "Click to offer xp points in current trade.\n" +ChatColor.GRAY + "IE /trade xp 100"),
				ClickEvent.SUGGEST_COMMAND,
				"/trade xp "));
		im.sendTo(player);
		player.closeInventory();
	}

	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {

	}

	public boolean ignoreForeignItems() {
		return true;
	}

	private boolean initiatorHasEmptyTrade() {
		return (getInitiatorsItems().isEmpty() && initiatorMoneyXP.getXPOffered() < 1 && initiatorMoneyXP.getMoneyOffered() < 1);
	}

	private boolean inviteeHasEmptyTrade() {
		return (getInviteesItems().isEmpty() && inviteeMoneyXP.getXPOffered() < 1 && inviteeMoneyXP.getMoneyOffered() < 1);
	}


	public void checkForSuccess() {
		if(initiatorAccept.isAccepted() && inviteeAccept.isAccepted()) {
			completeTrade();
		}
	}

	public boolean indexIsInInitiatorItemArea(int clicked) {
		for(int index : initiatorItemArea) {
			if(index == clicked) return true;
		}
		return false;
	}

	public boolean indexIsInInviteeItemArea(int clicked) {
		for(int index : inviteeItemArea) {
			if(index == clicked) return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void cancelTrade() {
		this.isEnded = true;
		TradeManager.getInstance().removeTradeSession(this);
		Player initiatorPlayer = Bukkit.getPlayer(initiatorName);
		if(initiatorPlayer != null && initiatorPlayer.isOnline()) {
			initiatorPlayer.closeInventory();
			initiatorPlayer.sendMessage(ChatColor.YELLOW + "[TradingPost] Your trade session with " + inviteeName + " has been canceled. Any items you had for offer will be mailed to you.");
		}
		Player inviteePlayer = Bukkit.getPlayer(inviteeName);
		if(inviteePlayer != null && inviteePlayer.isOnline()) {
			inviteePlayer.closeInventory();
			inviteePlayer.sendMessage(ChatColor.YELLOW + "[TradingPost] Your trade session with " + initiatorName + " has been canceled. Any items you had for offer will be mailed to you.");
		}
		List<ItemStack> initiatorItems = this.getInitiatorsItems();
		if(!initiatorItems.isEmpty()) {
			Mail initiatorMail = new TradeGoods(initiatorName, "the Trade Master", "You recently had a canceled trade with " + inviteeName + ". Returned are the items you had for offer.", Time.getTime(), MailStatus.UNREAD, UUID.randomUUID(), initiatorItems, 0, 0);
			MailboxManager.getInstance().getPlayerMailbox(initiatorName).receiveMail(initiatorMail);
		}
		List<ItemStack> inviteeItems = this.getInviteesItems();
		if(!inviteeItems.isEmpty()) {
			Mail mail = new TradeGoods(inviteeName, "the Trade Master", "You recently had a canceled trade with " + initiatorName + ". Returned are the items you had for offer.", Time.getTime(), MailStatus.UNREAD, UUID.randomUUID(), inviteeItems, 0, 0);
			MailboxManager.getInstance().getPlayerMailbox(inviteeName).receiveMail(mail);
		}
	}

	@SuppressWarnings("deprecation")
	public void completeTrade() {
		this.isEnded = true;
		TradeManager.getInstance().removeTradeSession(this);
		Player initiatorPlayer = Bukkit.getPlayer(initiatorName);
		if(initiatorPlayer != null && initiatorPlayer.isOnline()) {
			initiatorPlayer.closeInventory();
			initiatorPlayer.sendMessage(ChatColor.YELLOW + "[TradingPost] Your trade session with " + inviteeName + " was successful! All traded goods have been mailed to you.");
		}
		Player inviteePlayer = Bukkit.getPlayer(inviteeName);
		if(inviteePlayer != null && inviteePlayer.isOnline()) {
			inviteePlayer.closeInventory();
			inviteePlayer.sendMessage(ChatColor.YELLOW + "[TradingPost] Your trade session with " + initiatorName + " was successful! All traded goods have been mailed to you.");
		}
		List<ItemStack> initiatorItems = this.getInitiatorsItems();
		if(initiatorMoneyXP.getMoneyOffered() > 0) {
			Mythsentials.economy.withdrawPlayer(initiatorName, initiatorMoneyXP.getMoneyOffered());
		}
		if(initiatorMoneyXP.getXPOffered() > 0) {
			long totalXp = SetExpFix.getTotalExperience(Bukkit.getPlayer(initiatorName)) - initiatorMoneyXP.getXPOffered();
			if (totalXp < 0L)
			{
				totalXp = 0L;
			}
			SetExpFix.setTotalExperience(Bukkit.getPlayer(initiatorName), (int)totalXp);
		}
		Mail tradeGoodsForInvitee = new TradeGoods(inviteeName, "the Trade Master", "You recently had a successful trade with " + initiatorName + ". Here are the goods you got from that trade.", Time.getTime(), MailStatus.UNREAD, UUID.randomUUID(), initiatorItems, initiatorMoneyXP.getMoneyOffered(), initiatorMoneyXP.getXPOffered());
		MailboxManager.getInstance().getPlayerMailbox(inviteeName).receiveMail(tradeGoodsForInvitee);

		List<ItemStack> inviteeItems = this.getInviteesItems();
		if(inviteeMoneyXP.getMoneyOffered() > 0) {
			Mythsentials.economy.withdrawPlayer(inviteeName, inviteeMoneyXP.getMoneyOffered());
		}
		if(inviteeMoneyXP.getXPOffered() > 0) {
			long totalXp = SetExpFix.getTotalExperience(Bukkit.getPlayer(inviteeName)) - inviteeMoneyXP.getXPOffered();
			if (totalXp < 0L)
			{
				totalXp = 0L;
			}
			SetExpFix.setTotalExperience(Bukkit.getPlayer(inviteeName), (int)totalXp);
		}
		Mail tradeGoodsForInitiator = new TradeGoods(initiatorName, "the Trade Master", "You recently had a successful trade with " + inviteeName + ". Here are the goods you got from that trade.", Time.getTime(), MailStatus.UNREAD, UUID.randomUUID(), inviteeItems, inviteeMoneyXP.getMoneyOffered(), inviteeMoneyXP.getXPOffered());
		MailboxManager.getInstance().getPlayerMailbox(initiatorName).receiveMail(tradeGoodsForInitiator);
	}

	public List<ItemStack> getInitiatorsItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(int index : this.initiatorItemArea) {
			ItemStack item = inventory.getItem(index);
			if(item != null && item.getType() != Material.AIR) {
				items.add(item);
			}
		}
		return items;
	}

	public List<ItemStack> getInviteesItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(int index : this.inviteeItemArea) {
			ItemStack item = inventory.getItem(index);
			if(item != null && item.getType() != Material.AIR) {
				items.add(item);
			}
		}
		return items;
	}

	public void playerOffersMoney(String playerName, double money) {
		if(playerName.equals(initiatorName)) {
			this.initiatorMoneyXP.setMoneyOffered(money);
			this.updateTradeButton(TradeButton.INITIATOR_MONEYXP);
			this.initiatorLocked.setTradeIsEmpty(this.initiatorHasEmptyTrade());
			this.updateTradeButton(TradeButton.INITIATOR_LOCKIN);
		}
		else if(playerName.equals(inviteeName)) {
			this.inviteeMoneyXP.setMoneyOffered(money);
			this.updateTradeButton(TradeButton.INVITEE_MONEYXP);
			this.inviteeLocked.setTradeIsEmpty(this.inviteeHasEmptyTrade());
			this.updateTradeButton(TradeButton.INVITEE_LOCKIN);
		}
	}

	public boolean playerIsLockedIn(String playerName) {
		if(playerName.equals(initiatorName) && initiatorLocked.isLocked()) {
			return true;
		}
		else if(playerName.equals(inviteeName) && inviteeLocked.isLocked()){
			return true;
		}
		return false;
	}

	public void playerOffersXP(String playerName, int xp) {
		if(playerName.equals(initiatorName)) {
			this.initiatorMoneyXP.setXPOffered(xp);
			this.updateTradeButton(TradeButton.INITIATOR_MONEYXP);
			this.initiatorLocked.setTradeIsEmpty(this.initiatorHasEmptyTrade());
			this.updateTradeButton(TradeButton.INITIATOR_LOCKIN);
		}
		else if(playerName.equals(inviteeName)) {
			this.inviteeMoneyXP.setXPOffered(xp);
			this.updateTradeButton(TradeButton.INVITEE_MONEYXP);
			this.inviteeLocked.setTradeIsEmpty(this.inviteeHasEmptyTrade());
			this.updateTradeButton(TradeButton.INVITEE_LOCKIN);
		}
	}

	public void updateTradeButton(TradeButton button) {
		switch(button) {
			case INITIATOR_ACCEPT:
				inventory.setItem(38, initiatorAccept.toItemStack());
				break;
			case INITIATOR_LOCKIN:
				inventory.setItem(37, initiatorLocked.toItemStack());
				break;
			case INITIATOR_MONEYXP:
				inventory.setItem(36, initiatorMoneyXP.toItemStack());
				break;
			case INVITEE_ACCEPT:
				inventory.setItem(42, inviteeAccept.toItemStack());
				break;
			case INVITEE_LOCKIN:
				inventory.setItem(43, inviteeLocked.toItemStack());
				break;
			case INVITEE_MONEYXP:
				inventory.setItem(44, inviteeMoneyXP.toItemStack());
				break;
			case TRADE_LOG:
				inventory.setItem(31, tradeLog.toItemStack());
				break;
			default:
				break;
		}
	}

	public enum TradeButton {
		INITIATOR_LOCKIN, INITIATOR_ACCEPT, INITIATOR_MONEYXP, INVITEE_LOCKIN, INVITEE_ACCEPT, INVITEE_MONEYXP, TRADE_LOG
	}

	public String getInitiatorName() {
		return initiatorName;
	}

	public boolean hasViewers() {
		return !inventory.getViewers().isEmpty();
	}

	public String getInviteeName() {
		return inviteeName;
	}

	public boolean isEnded() {
		return isEnded;
	}*/

}
