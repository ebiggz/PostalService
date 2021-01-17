package com.ebiggz.postalservice.tradingpost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.ebiggz.postalservice.apis.guiAPI.GUIManager;


public class TradeManager {

	private List<TradeSession> activeTrades = new ArrayList<TradeSession>();

	/* Singleton */
	private static TradeManager instance = null;
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

	public List<Player> availablePlayers(Player player) {
		return Bukkit.getOnlinePlayers().stream()
				.filter(p -> p != player && !this.playerHasActiveTradeSession(p))
				.collect(Collectors.toList());
	}

}
