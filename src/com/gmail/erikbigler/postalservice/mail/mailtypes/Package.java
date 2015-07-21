package com.gmail.erikbigler.postalservice.mail.mailtypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.Utils;

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
			throw new MailException("You must have items in your drop box to send a Package!");
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
			throw new MailException("There is not enough empty room in your inventory. Please clear space and try again.");
		} else {
			for(ItemStack item : items) {
				player.getInventory().addItem(item);
			}
		}
	}

	@Override
	public String getAttachmentClaimMessage() {
		return "You have claimed the contents of this package.";
	}

	@Override
	public String getAttachmentDescription() {
		return items.size() + " item(s)";
	}

	@Override
	public boolean useSummaryScreen() {
		return true;
	}

	@Override
	public String getSummaryScreenTitle() {
		return "Package Contents";
	}

	@Override
	public String getSummaryClaimButtonTitle() {
		return "Claim Package";
	}

	@Override
	public ItemStack[] getSummaryIcons() {
		ItemStack[] itemArray = new ItemStack[items.size()];
		items.toArray(itemArray);
		return itemArray;
	}

	@Override
	public MailType clone() {
		return new Package();
	}
}
