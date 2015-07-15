package com.gmail.erikbigler.postalservice.mail;

import java.util.ArrayList;
import java.util.List;

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
		for (MailType mailType : mailTypes) {
			if (mailType.getName().equalsIgnoreCase(newType.getName())) {
				// log error
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
			mailTypeNames[i] = mailTypes.get(i).getName();
		}
		return mailTypeNames;
	}

	public MailType getMailType(String name) {
		for (MailType mailType : mailTypes) {
			if (mailType.getName().equalsIgnoreCase(name))
				return mailType;
		}
		return null;
	}

	public enum BoxType {
		SENT, INBOX
	}

}
