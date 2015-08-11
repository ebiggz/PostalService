package com.ebiggz.postalservice.tradingpost;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.ebiggz.postalservice.apis.guiAPI.GUIButton;


public class LockOfferButton extends GUIButton {

	private boolean isLocked = false;
	private boolean tradeIsEmpty = true;
	private String playerName;

	public LockOfferButton(String playerName) {
		super(Material.CHEST,
				ChatColor.YELLOW +""+ ChatColor.BOLD + playerName + " Lock Offer Button",
				Arrays.asList(
						ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Can't lock",
						ChatColor.GRAY + "(" + playerName + "'s trade cannot be empty)"));
		this.playerName = playerName;
	}

	public void shouldLock(boolean shouldLock) {
		this.isLocked = shouldLock;
		if(shouldLock) {
			this.setBaseIcon(Material.ENDER_CHEST);
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Locked in",
							ChatColor.GRAY + "* " + playerName + ", click this to",
							ChatColor.GRAY + "unlock your trade offer. *"));
		} else {
			this.setBaseIcon(Material.CHEST);
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Unlocked",
							ChatColor.GRAY + "* " + playerName + ", click this to",
							ChatColor.GRAY + "lock your trade offer. *"));
		}
	}

	public boolean tradeIsEmpty() {
		return this.tradeIsEmpty;
	}

	public void setTradeIsEmpty(boolean isEmpty) {
		this.tradeIsEmpty = isEmpty;
		if(!this.isLocked) {
			if(isEmpty) {
				this.setLore(
						Arrays.asList(
								ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Can't lock",
								ChatColor.GRAY + "(" + playerName + "'s trade cannot be empty)"));
			} else {
				this.setLore(
						Arrays.asList(
								ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Unlocked",
								ChatColor.GRAY + "* " + playerName + ", click this to",
								ChatColor.GRAY + "lock your trade offer. *"));
			}
		}
	}

	public boolean isLocked() {
		return this.isLocked;
	}

	public String getPlayerName() {
		return this.playerName;
	}
}
