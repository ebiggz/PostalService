package com.gmail.erikbigler.postalservice.mail;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.exceptions.MailException;


public interface MailType {

	/** Identifiers are used places such as when saved to the database. Identifiers should be static.
	 *
	 * Identifiers must be unique from other mail types. If a two mail types
	 * share the same identifier, the second one will not be registered.
	 *
	 * @return The mail types internal identifier
	 */
	public String getIdentifier();

	/** Display names are what is shown to users, what is used for the "postalservice.mail.send.[mailtype]"
	 * permission, and what is used for the "enabled-mail-types" section. Display names can be dynamic, allowing
	 * server administrators to translate the display names.
	 *
	 * Display names must also be unique from other mail types.
	 *
	 * @return The mail types display name
	 */
	public String getDisplayName();

	/**
	 * @return The icon shown in the inbox and sent box screens for a mail type
	 */
	public Material getIcon();

	/** The hover over description is what is shown when a player hovers over the mail type name in
	 *  the interactive chat compose message.
	 *
	 * @return The hover over description
	 */
	public String getHoveroverDescription();

	/** Whether or not players should be required to include a text message (with the "message:" node) while
	 * composing this mail type.
	 *
	 * @return Whether or not players should be required to include a text message
	 */
	public boolean requireMessage();

	/** This is the argument players must use to specify what they want to attach while composing this mail type.
	 *
	 * For example, the Payment mail type returns "amount" so a player would need to include "amount:" in their compose
	 * command.
	 *
	 * If you do not need an attachment argument, simply return null.
	 *
	 * @return The command argument or null if not needed.
	 */
	public String getAttachmentCommandArgument();

	/** This method is called when the player issues the send command for this mail type.
	 *
	 * This is where you should have logic to verify what the player is sending and, if all is well,
	 * return the data in string form to be saved to the database in a format that you can parse later.
	 *
	 *  @param sender
	 *            The player who issued the command.
	 *         commandArgs
	 *            Any command arguments following after the provided attachment command argument
	 *  @throws MailException
	 *            You should throw a MailException if there any issues with the provided arguments or the player doens't
	 *            meet requirements to send (IE they don't have the money they are trying to send). You can pass a string
	 *            while instantiating a MailException. This string is shown to the player as the error message.
	 *
	 * @return The computed attachment data as a string to be saved to the database.
	 */
	public String handleSendCommand(Player sender, String[] commandArgs) throws MailException;

	/** This method is called when the mail type is being loaded from the database.
	 *
	 * This is where you should parse the string that was returned from the handleSendCommand and save it
	 * to whatever format you need in a private class field.
	 *
	 *  @param attachmentData
	 *            The string containing any attachment data provided by handleSendCommand
	 */
	public void loadAttachments(String attachmentData);

	/** This method is called when a player is claiming the attachments of the mail type.
	 *
	 *  @param Player
	 *            The player claiming this mail type
	 *
	 *  @throws MailException
	 *            You should throw a MailException if there any issues when administering the attachment to the player
	 *            (IE they don't have enough space in their inventory). You can pass a string
	 *            while instantiating a MailException. This string is shown to the player as the error message.
	 */
	public void administerAttachments(Player player) throws MailException;

	/** This message is shown to the player if they successfully claimed the attachments of this mail type.
	 *
	 *  @return The successful attachment claim message.
	 */
	public String getAttachmentClaimMessage();

	/** This message is shown under the mail type name when viewing the mail in the inbox/sent views.
	 *
	 * It allows players to see what they will get if the claim the attachment.
	 *
	 * For example, if $10 dollars was attached, it might say "$10"
	 *
	 *  @return The attachment description
	 */
	public String getAttachmentDescription();

	/** Sometimes you cannot adequately describe the attachments with a string message. In those cases,
	 * You can opt to use the summary Screen. An example of this is the Package mail type, where it uses
	 * the Summary Screen to show the items in the package.
	 *
	 *  @return Whether or not the player should be taken to a Summary Screen when claiming this mail type.
	 */
	public boolean useSummaryScreen();

	/** This is the title that will be shown on the top of the Summary Screen
	 *
	 *  @return The title of the Summary Screen
	 */
	public String getSummaryScreenTitle();

	/** This is the title of the button at the bottom of the Summary Screen that the players use to claim the attachment.
	 *
	 *  @return The title of the claim button
	 */
	public String getSummaryClaimButtonTitle();

	/** Here you can return the array of items to be shown in the summary screen.
	 *
	 * For example, the Package mail type returns all the items contained in the package.
	 *
	 *  @return The items to be shown in the Summary Screen.
	 */
	public ItemStack[] getSummaryIcons();

}
