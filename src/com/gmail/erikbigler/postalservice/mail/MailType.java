package com.gmail.erikbigler.postalservice.mail;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.exceptions.MailException;


public interface MailType extends Cloneable {

	public String getIdentifier();

	public String getDisplayName();

	public Material getIcon();

	public String getHoveroverDescription();

	public boolean requireMessage();

	public String getAttachmentCommandArgument();

	public String handleSendCommand(Player sender, String[] commandArgs) throws MailException;

	public void loadAttachments(String attachmentData);

	public void administerAttachments(Player player) throws MailException;

	public String getAttachmentClaimMessage();

	public String getAttachmentDescription();

	public boolean useSummaryScreen();

	public String getSummaryScreenTitle();

	public String getSummaryClaimButtonTitle();

	public ItemStack[] getSummaryIcons();

}
