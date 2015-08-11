package com.ebiggz.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.utils.SetExpFix;


public class Experience implements MailType {

	private int amount;

	@Override
	public String getIdentifier() {
		return "XP";
	}
	@Override
	public String getDisplayName() {
		return Phrases.MAILTYPE_EXPERIENCE.toString();
	}

	@Override
	public String getHoveroverDescription() {
		return Phrases.MAILTYPE_EXPERIENCE_HOVERTEXT.toString();
	}

	@Override
	public boolean requireMessage() {
		return false;
	}

	@Override
	public String getAttachmentCommandArgument() {
		return Phrases.COMMAND_ARG_AMOUNT.toString();
	}

	@Override
	public String handleSendCommand(Player sender, String[] commandArgs)
			throws MailException {
		if(commandArgs == null || commandArgs.length < 1) {
			throw new MailException(Phrases.ERROR_MAILTYPE_EXPERIENCE_EMPTY.toString());
		} else {
			try {
				int amount = Integer.parseInt(commandArgs[0]);
				if(sender.getTotalExperience() < amount) {
					throw new MailException(Phrases.ERROR_MAILTYPE_EXPERIENCE_NOTENOUGH.toString());
				}
				long totalXp = SetExpFix.getTotalExperience(sender) - amount;
				if (totalXp < 0L)
				{
					totalXp = 0L;
				}
				SetExpFix.setTotalExperience(sender, (int)totalXp);

				return commandArgs[0];
			} catch (NumberFormatException e) {
				if(Config.ENABLE_DEBUG) e.printStackTrace();
				throw new MailException(Phrases.ERROR_MAILTYPE_EXPERIENCE_NOTVALID.toString());
			}
		}
	}

	@Override
	public void loadAttachments(String attachmentData) {
		try {
			amount = Integer.parseInt(attachmentData);
		} catch (Exception e) {if(Config.ENABLE_DEBUG) e.printStackTrace();}
	}

	@Override
	public void administerAttachments(Player player) throws MailException {
		long xp = amount + SetExpFix.getTotalExperience(player);
		if (xp > 2147483647L)
		{
			xp = 2147483647L;
		}
		if (xp < 0L)
		{
			xp = 0L;
		}
		SetExpFix.setTotalExperience(player, (int)xp);
	}

	@Override
	public String getAttachmentClaimMessage() {
		return Phrases.ALERT_MAILTYPE_EXPERIENCE_CLAIM.toString();
	}

	@Override
	public Material getIcon() {
		return Material.EXP_BOTTLE;
	}

	@Override
	public String getAttachmentDescription() {
		return Phrases.MAILTYPE_EXPERIENCE_ITEMDESC.toString().replace("%count%", Integer.toString(amount));
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
