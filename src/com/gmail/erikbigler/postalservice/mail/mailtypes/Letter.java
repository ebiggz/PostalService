package com.gmail.erikbigler.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.MailType;


public class Letter implements MailType {

	@Override
	public String getName() {
		return "Letter";
	}

	@Override
	public String getAttachmentCommandArgument() {
		return null;
	}

	@Override
	public String handleSendCommand(Player sender, String[] commandArgs)
			throws MailException {
		return null;
	}

	@Override
	public void loadAttachments(String attachmentData) {


	}

	@Override
	public void administerAttachments(Player player)
			throws MailException {
	}

	@Override
	public Material getIcon() {
		return Material.PAPER;
	}

	@Override
	public String getAttachmentDescription() {
		return null;
	}

	@Override
	public boolean useSummaryScreen() {
		return false;
	}

	@Override
	public ItemStack[] getAttachmentIcons() {
		return null;
	}
}
