package com.ebiggz.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.PostalService;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.MailType;

public class Payment implements MailType {

	private double amount;

	@Override
	public String getIdentifier() {
		return "Payment";
	}

	@Override
	public String getDisplayName() {
		return Phrases.MAILTYPE_PAYMENT.toString();
	}

	@Override
	public Material getIcon() {
		return Material.GOLD_INGOT;
	}

	@Override
	public String getHoveroverDescription() {
		return Phrases.MAILTYPE_PAYMENT_HOVERTEXT.toString();
	}

	@Override
	public boolean requireMessage() {
		return false;
	}

	@Override
	public String getAttachmentCommandArgument() {
		return Phrases.COMMAND_ARG_AMOUNT.toString();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String handleSendCommand(Player sender, String[] commandArgs) throws MailException {
		double money = 0;
		try {
			money = Double.parseDouble(commandArgs[0]);
		} catch (Exception e) {
			throw new MailException(Phrases.ERROR_MAILTYPE_PAYMENT_NOTVALID.toString());
		}
		if(money <= 0) {
			throw new MailException(Phrases.ERROR_MAILTYPE_PAYMENT_EMPTY.toString());
		}

		double balance = PostalService.economy.getBalance(sender.getName());

		if(balance >= money) {
			PostalService.economy.withdrawPlayer(sender.getName(), money);
		} else {
			throw new MailException(Phrases.ERROR_MAILTYPE_PAYMENT_NOTENOUGH.toString());
		}
		return Double.toString(money);
	}

	@Override
	public void loadAttachments(String attachmentData) {
		try {
			amount = Double.parseDouble(attachmentData);
		} catch (Exception e){
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void administerAttachments(Player player) throws MailException {
		PostalService.economy.depositPlayer(player.getName(), amount);
	}

	@Override
	public String getAttachmentClaimMessage() {
		return Phrases.ALERT_MAILTYPE_PAYMENT_CLAIM.toString();
	}

	@Override
	public String getAttachmentDescription() {
		return Phrases.MAILTYPE_PAYMENT_ITEMDESC.toString().replace("%count%", Double.toString(amount));
	}

	@Override
	public boolean useSummaryScreen() {
		return false;
	}

	@Override
	public String getSummaryScreenTitle() {
		return null;
	}

	@Override
	public String getSummaryClaimButtonTitle() {
		return null;
	}

	@Override
	public ItemStack[] getSummaryIcons() {
		return null;
	}
}
