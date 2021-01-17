package com.ebiggz.postalservice.tradingpost;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.apis.guiAPI.GUIButton;

//@SuppressWarnings("deprecation")
public class AcceptOfferButton extends GUIButton {

	private boolean isAccepted = false;
	private boolean isWaiting = true;
	private String playerName;
	private String otherPlayer;

	private ItemStack unaccepted = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
	private ItemStack accepted = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

	public AcceptOfferButton(String playerName, String otherPlayer) {
		super(new ItemStack(Material.WHITE_STAINED_GLASS_PANE),
				ChatColor.YELLOW +""+ChatColor.BOLD + playerName + " Accept Offer Button",
				Arrays.asList(
						ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Can't accept",
						ChatColor.GRAY + "(Waiting for " + otherPlayer + " to",ChatColor.GRAY + "lock in their offer)"));
		this.playerName = playerName;
		this.otherPlayer = otherPlayer;
	}

	public void setAccept(boolean shouldAccept) {
		this.isAccepted = shouldAccept;
		if(shouldAccept) {
			this.setBaseIcon(accepted);
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Accepted",
							ChatColor.GRAY + "* " + playerName + ", click this to",
							ChatColor.GRAY + "unaccept " + otherPlayer + "'s offer *"));
		} else {
			this.setBaseIcon(unaccepted);
			if(isWaiting) {
				this.setLore(
						Arrays.asList(
								ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Can't accept",
								ChatColor.GRAY + "(Waiting for " + otherPlayer + " to",ChatColor.GRAY + "lock in their offer)"));
			} else {
				this.setLore(
						Arrays.asList(
								ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Not accepted",
								ChatColor.GRAY + "* " + playerName + ", click this to",
								ChatColor.GRAY + "accept " + otherPlayer + "'s offer *"));
			}
		}
	}

	public boolean isAccepted() {
		return this.isAccepted;
	}

	public boolean isWaiting() {
		return this.isWaiting;
	}

	public void setWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
		if(isAccepted) return;
		if(isWaiting) {
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Can't accept",
							ChatColor.GRAY + "(Waiting for " + otherPlayer + " to",ChatColor.GRAY + "lock in their offer)"));
		} else {
			this.setLore(
					Arrays.asList(
							ChatColor.WHITE + "Current Status: " + ChatColor.GOLD + "Not accepted",
							ChatColor.GRAY + "* " + playerName + ", click this to",
							ChatColor.GRAY + "accept " + otherPlayer + "'s offer *"));
		}
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public String getOtherPlayer() {
		return this.otherPlayer;
	}
}