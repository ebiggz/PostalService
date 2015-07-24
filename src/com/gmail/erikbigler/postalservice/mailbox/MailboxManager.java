package com.gmail.erikbigler.postalservice.mailbox;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException;
import com.gmail.erikbigler.postalservice.exceptions.MailboxException.Reason;
import com.gmail.erikbigler.postalservice.permissions.PermissionHandler;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class MailboxManager {

	public HashMap<Player, MailboxSelect> mailboxSelectors = new HashMap<Player, MailboxSelect>();
	public List<Player> willDropBook = new ArrayList<Player>();
	private List<Mailbox> mailboxes = new ArrayList<Mailbox>();

	protected MailboxManager() {
		/* exists to block instantiation */ }

	private static MailboxManager instance = null;

	public static MailboxManager getInstance() {
		if(instance == null) {
			instance = new MailboxManager();
		}
		return instance;
	}

	public enum MailboxSelect {
		ADD, REMOVE, OWNERSET
	}

	public boolean locationHasMailbox(Location location) {
		Mailbox mb = this.getMailbox(location);
		return mb != null;
	}

	public void loadMailboxes() {
		this.mailboxes.clear();
		if(Config.USE_DATABASE) {
			try {
				ResultSet rs = PostalService.getPSDatabase().querySQL("SELECT * FROM ps_mailboxes");
				while(rs.next()) {
					int id = rs.getInt("MailboxID");
					String location = rs.getString("Location");
					String playerIdentifier = rs.getString("PlayerID");
					if(location != null || playerIdentifier != null) {
						mailboxes.add(new Mailbox(UserFactory.getUserFromIdentifier(playerIdentifier), Utils.stringToLocation(playerIdentifier), id));
					}
				}
			} catch (Exception e) {
				if(Config.ENABLE_DEBUG)
					e.printStackTrace();
			}
		}
	}

	public Mailbox getMailbox(Location loc) {
		for(Mailbox mailbox : mailboxes) {
			if(mailbox.getLocation().equals(loc))
				return mailbox;
		}
		return null;
	}

	public void addMailboxAtLoc(Location location, Player player) throws MailboxException {
		if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(!PermissionHandler.playerCanCreateMailboxAtLoc(location, player)) {
			throw new MailboxException(Reason.NO_PERMISSION);
		} else if(this.locationHasMailbox(location)) {
			throw new MailboxException(Reason.ALREADY_EXISTS);
		} else {
			// savemailbox
		}
	}

	public void removeMailboxAtLoc(Location location, Player player) throws MailboxException {

		Mailbox mb = this.getMailbox(location);
		/*
		 * if(mythian.getMailboxLocs().size() >= getMaxMailboxCount(owner)) {
		 * throw new MailException(Reason.MAX_MAILBOXES_REACHED); }
		 */
		if(mb == null) {
			throw new MailboxException(Reason.DOESNT_EXIST);
		} else if(location.getBlock() != null && location.getBlock().getType() != Material.CHEST) {
			throw new MailboxException(Reason.NOT_CHEST);
		} else if(!mb.getOwner().getPlayerName().equals(player.getName())) {
			throw new MailboxException(Reason.NOT_OWNER);
		} else {
			// delete mailbox
		}
	}

	public void removeAllMailboxes(String owner) {

	}

	public boolean mailboxIsNearby(Location location, int distance) {
		for(Mailbox mailbox : mailboxes) {
			if(location.distance(mailbox.getLocation()) < distance)
				return true;
		}
		return false;
	}
}
