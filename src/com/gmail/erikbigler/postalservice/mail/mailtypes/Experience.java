package com.gmail.erikbigler.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.SetExpFix;


public class Experience implements MailType {

	private int amount;

	@Override
	public String getName() {
		return "XP";
	}

	@Override
	public String getAttachmentCommandArgument() {
		return "amount";
	}

	@Override
	public String handleSendCommand(Player sender, String[] commandArgs)
			throws MailException {
		if(commandArgs.length < 1) {
			throw new MailException("You must include an xp amount!");
		} else {
			try {
				int amount = Integer.parseInt(commandArgs[0]);
				if(sender.getTotalExperience() < amount) {
					throw new MailException("You don't have that amount of XP to send!");
				}

				long totalXp = SetExpFix.getTotalExperience(sender) - amount;
				if (totalXp < 0L)
				{
					totalXp = 0L;
				}
				SetExpFix.setTotalExperience(sender, (int)totalXp);

				return commandArgs[0];
			} catch (Exception e) {
				throw new MailException("That is not a valid xp amount!");
			}
		}
	}

	@Override
	public void loadAttachments(String attachmentData) {
		try {
			amount = Integer.parseInt(attachmentData);
		} catch (Exception e) {}
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
	public Material getIcon() {
		return Material.EXP_BOTTLE;
	}

	@Override
	public String getAttachmentDescription() {
		return amount + " XP point(s)";
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
