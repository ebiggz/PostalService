package com.gmail.erikbigler.postalservice.tradingpost;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;


public class TradeManager {

	private List<TradeSession> activeTrades = new ArrayList<TradeSession>();

	/* Singleton */
	/*private static TradeManager instance = null;
	private TradeManager() {}
	public static TradeManager getInstance() {
		if(instance == null) {
			instance = new TradeManager();
		}
		return instance;
	}

	public void startTradeSession(Player initiator, Player invitee) {
		TradeSession session = new TradeSession(initiator.getName(), invitee.getName());
		GUIManager.getInstance().showGUI(session, initiator);
		activeTrades.add(session);
		invitee.sendMessage(ChatColor.YELLOW + "[TradingPost] " + ChatColor.AQUA + initiator.getName() + ChatColor.YELLOW + " has opened up a Trade Session with you! Visit your nearest mailbox to view it.");
	}

	public void removeTradeSession(TradeSession session) {
		activeTrades.remove(session);
	}

	public TradeSession getPlayersActiveTradeSession(Player player) {
		for(TradeSession session : activeTrades) {
			if(session.getInitiatorName().equals(player.getName()) || session.getInviteeName().equals(player.getName())) return session;
		}
		return null;
	}

	public boolean playerHasActiveTradeSession(Player player) {
		for(TradeSession session : activeTrades) {
			if(session.getInitiatorName().equals(player.getName()) || session.getInviteeName().equals(player.getName())) return true;
		}
		return false;
	}

	public List<String> availablePlayers(Player player) {
		List<String> availablePlayers = new ArrayList<String>();
		for(Player otherPlayer : Bukkit.getOnlinePlayers()) {
			System.out.println("Checking for: " + otherPlayer.getName());
			if(otherPlayer == player) {
				System.out.println("Same player! Skipping! ");
				continue;
			}
			if(this.playerHasActiveTradeSession(otherPlayer)) {
				System.out.println("Already in a sesson!");
				continue;
			}
			availablePlayers.add(otherPlayer.getName());
		}
		System.out.println("Players: ");
		for(String p : availablePlayers) {
			System.out.println(p);
		}
		return availablePlayers;
	}*/

}
