package com.gmail.erikbigler.postalservice.mail;

import java.util.ArrayList;
import java.util.List;

import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.mail.Mail.MailStatus;

public class MailManager {

	private List<MailType> mailTypes = new ArrayList<MailType>();

	protected MailManager() { /* exists to block instantiation */
	}

	private static MailManager instance = null;

	public static MailManager getInstance() {
		if (instance == null) {
			instance = new MailManager();
		}
		return instance;
	}

	public void registerMailType(MailType newType) {
		if (Config.mailTypeIsDisabled(newType)) return;
		for (MailType mailType : mailTypes) {
			if (mailType.getDisplayName().equalsIgnoreCase(newType.getDisplayName()) || mailType.getIdentifier().equalsIgnoreCase(newType.getIdentifier())) {
				// TODO log warning error
				return;
			}
		}
		mailTypes.add(newType);
	}

	public void deregisterMailType(MailType mailType) {
		mailTypes.remove(mailType);
	}

	public String[] getMailTypeNames() {
		String[] mailTypeNames = new String[mailTypes.size()];
		for (int i = 0; i < mailTypes.size(); i++) {
			mailTypeNames[i] = mailTypes.get(i).getDisplayName();
		}
		return mailTypeNames;
	}

	public MailType[] getMailTypes() {
		MailType[] types = new MailType[mailTypes.size()];
		mailTypes.toArray(types);
		return types;
	}

	public String[] getMailTypeIdentifiers() {
		String[] mailTypeNames = new String[mailTypes.size()];
		for (int i = 0; i < mailTypes.size(); i++) {
			mailTypeNames[i] = mailTypes.get(i).getIdentifier();
		}
		return mailTypeNames;
	}

	public MailType getMailTypeByName(String name) {
		for (MailType mailType : mailTypes) {
			if (mailType.getDisplayName().equalsIgnoreCase(name))
				return mailType.clone();
		}
		return null;
	}

	public MailType getMailTypeByIdentifier(String identifier) {
		for (MailType mailType : mailTypes) {
			if (mailType.getIdentifier().equalsIgnoreCase(identifier))
				return mailType.clone();
		}
		return null;
	}

	public MailStatus getMailStatusFromID(int id) {
		switch(id) {
		case 0:
			return MailStatus.UNREAD;
		case 1:
			return MailStatus.READ;
		case 2:
			return MailStatus.CLAIMED;
		default:
			return null;
		}
	}

	public int getIDforMailStatus(MailStatus status) {
		switch(status) {
		case CLAIMED:
			return 2;
		case READ:
			return 1;
		case UNREAD:
			return 0;
		}
		return 0;
	}

	public enum BoxType {
		SENT, INBOX
	}

}
