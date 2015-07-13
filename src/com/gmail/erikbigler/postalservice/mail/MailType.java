package com.gmail.erikbigler.postalservice.mail;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.exceptions.MailException;


public interface MailType {

	public String getName();

	public String getAttachmentCommandArgument();

	public String handleSendCommand(Player sender, String[] commandArgs) throws MailException;

	public void loadAttachments(String attachmentData);

	public void administerAttachments(Player player) throws MailException;

	public Material getIcon();

	public String getAttachmentDescription();

	public boolean useSummaryScreen();

	public ItemStack[] getAttachmentIcons();

}
