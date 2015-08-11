package com.ebiggz.postalservice.tradingpost;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.ebiggz.postalservice.apis.guiAPI.GUIButton;

public class TradeMoneyXpButton extends GUIButton {

	private double moneyOffered = 0.0;
	private int xpOffered = 0;
	private String playerName;

	public TradeMoneyXpButton(String playerName) {
		super(Material.GOLD_INGOT,
				ChatColor.YELLOW +""+ ChatColor.BOLD + playerName + " Money/XP Offers",
				Arrays.asList(
						ChatColor.WHITE + "Money: " + ChatColor.GOLD + "$0",
						ChatColor.WHITE + "XP: " + ChatColor.GOLD + "0",
						ChatColor.GRAY + "* " + playerName +", click here to",
						ChatColor.GRAY + "offer/change money or xp *"));
		this.playerName = playerName;
	}

	public double getMoneyOffered() {
		return this.moneyOffered;
	}

	public void setMoneyOffered(double money) {
		this.moneyOffered = money;
		this.updateItem();
	}

	public int getXPOffered() {
		return this.xpOffered;
	}

	public void setXPOffered(int xp) {
		this.xpOffered = xp;
		this.updateItem();
	}

	public String getPlayerName() {
		return this.playerName;
	}

	private void updateItem() {
		if(this.moneyOffered > 0 || this.xpOffered > 0) {
			this.shouldGlow(true);
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Money: " + ChatColor.GOLD + "$" + Double.toString(moneyOffered),
							ChatColor.WHITE + "XP: " + ChatColor.GOLD + Integer.toString(xpOffered),
							ChatColor.GRAY + "* " + playerName +", click here to",
							ChatColor.GRAY + "offer/change money or xp *"));
		}
	}
}
