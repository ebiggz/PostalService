package com.ebiggz.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.MailType;


public class Letter implements MailType {


	@Override
	public String getIdentifier() {
		return "Letter";
	}

	@Override
	public String getDisplayName() {
		return Phrases.MAILTYPE_LETTER.toString();
	}

	@Override
	public String getHoveroverDescription() {
		return Phrases.MAILTYPE_LETTER_HOVERTEXT.toString();
	}

	@Override
	public boolean requireMessage() {
		return true;
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
	public void loadAttachments(String attachmentData) {}

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
	public String getAttachmentClaimMessage() {
		return null;
	}

	@Override
	public boolean useSummaryScreen() {
		return false;
	}

	@Override
	public String getSummaryScreenTitle() {
		return "";
	}

	@Override
	public String getSummaryClaimButtonTitle() {
		return "";
	}

	@Override
	public ItemStack[] getSummaryIcons() {
		return null;
	}
}
