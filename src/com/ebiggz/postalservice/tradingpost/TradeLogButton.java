package com.ebiggz.postalservice.tradingpost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.ebiggz.postalservice.apis.guiAPI.GUIButton;

public class TradeLogButton extends GUIButton {

	List<String> logEntries = new ArrayList<String>();

	public TradeLogButton() {
		super(Material.BOOK_AND_QUILL, ChatColor.YELLOW +""+ ChatColor.BOLD + "Trade Log", Arrays.asList(ChatColor.GRAY + "..."));
	}

	public void addLogEntry(String text) {
		logEntries.add(0, text);
		List<String> trimmedEntries = new ArrayList<String>();
		int count = 0;
		for(String entry : logEntries) {
			if(count > 10) break;
			trimmedEntries.add("- " + entry);
			count++;
		}
		trimmedEntries.add(ChatColor.GRAY + "...");
		this.setLore(trimmedEntries);
	}


}
