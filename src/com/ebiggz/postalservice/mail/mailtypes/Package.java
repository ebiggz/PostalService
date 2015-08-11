package com.ebiggz.postalservice.mail.mailtypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ebiggz.postalservice.backend.User;
import com.ebiggz.postalservice.backend.UserFactory;
import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.Language.Phrases;
import com.ebiggz.postalservice.exceptions.MailException;
import com.ebiggz.postalservice.mail.MailType;
import com.ebiggz.postalservice.utils.Utils;

public class Package implements MailType {

	private List<ItemStack> items = new ArrayList<ItemStack>();

	@Override
	public String getIdentifier() {
		return "Package";
	}

	@Override
	public String getDisplayName() {
		return Phrases.MAILTYPE_PACKAGE.toString();
	}

	@Override
	public Material getIcon() {
		return Material.CHEST;
	}

	@Override
	public String getHoveroverDescription() {
		return Phrases.MAILTYPE_PACKAGE_HOVERTEXT.toString();
	}

	@Override
	public boolean requireMessage() {
		return false;
	}

	@Override
	public String getAttachmentCommandArgument() {
		return null;
	}

	@Override
	public String handleSendCommand(Player sender, String[] commandArgs) throws MailException {
		User user = UserFactory.getUser(sender.getName());
		List<ItemStack> dropBoxContents = user.getDropbox(Config.getCurrentWorldGroupForUser(user));
		if(dropBoxContents == null || dropBoxContents.isEmpty()) {
			throw new MailException(Phrases.ERROR_MAILTYPE_PACKAGE_NO_ITEMS.toString());
		} else {
			user.saveDropbox(null, Config.getCurrentWorldGroupForUser(user));
			return Arrays.toString(Utils.itemsToBytes(dropBoxContents));
		}
	}

	@Override
	public void loadAttachments(String attachmentData) {
		String[] byteValues = attachmentData.substring(1, attachmentData.length() - 1).split(",");
		byte[] bytes = new byte[byteValues.length];

		for (int i=0, len=bytes.length; i<len; i++) {
			bytes[i] = Byte.parseByte(byteValues[i].trim());
		}
		items = Utils.bytesToItems(bytes);
	}

	@Override
	public void administerAttachments(Player player) throws MailException {
		int openSpots = Utils.getPlayerOpenInvSlots(player);
		if(items.size() > openSpots) {
			throw new MailException(Phrases.ERROR_MAILTYPE_PACKAGE_NEED_SPACE.toString());
		} else {
			for(ItemStack item : items) {
				player.getInventory().addItem(item);
			}
		}
	}

	@Override
	public String getAttachmentClaimMessage() {
		return Phrases.ALERT_MAILTYPE_PACKAGE_CLAIM.toString();
	}

	@Override
	public String getAttachmentDescription() {
		return Phrases.MAILTYPE_PACKAGE_ITEMDESC.toString().replace("%count%", Integer.toString(items.size()));
	}

	@Override
	public boolean useSummaryScreen() {
		return true;
	}

	@Override
	public String getSummaryScreenTitle() {
		return Phrases.MAILTYPE_PACKAGE_SUMMARYSCREEN_TITLE.toString();
	}

	@Override
	public String getSummaryClaimButtonTitle() {
		return Phrases.MAILTYPE_PACKAGE_CLAIM_BUTTON.toString();
	}

	@Override
	public ItemStack[] getSummaryIcons() {
		ItemStack[] itemArray = new ItemStack[items.size()];
		items.toArray(itemArray);
		return itemArray;
	}
}
