package com.gmail.erikbigler.postalservice.mail;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.mail.Mail.MailStatus;

public class MailManager {

	private List<MailType> mailTypes = new ArrayList<MailType>();
	public List<Player> willDropBook = new ArrayList<Player>();

	protected MailManager() { /* exists to block instantiation */
	}

	private static MailManager instance = null;

	public static MailManager getInstance() {
		if(instance == null) {
			instance = new MailManager();
		}
		return instance;
	}

	public void registerMailType(MailType newType) {
		if(Config.mailTypeIsDisabled(newType))
			return;
		for(MailType mailType : mailTypes) {
			if(mailType.getDisplayName().equalsIgnoreCase(newType.getDisplayName()) || mailType.getIdentifier().equalsIgnoreCase(newType.getIdentifier())) {
				PostalService.getPlugin().getLogger().warning("A plugin attempted to register a mail type named " + newType.getDisplayName() + " with the identifier " + newType.getIdentifier() + " but a mail type with that name or identifier already exists!");
				return;
			}
		}
		mailTypes.add(newType);
		Permission permission = new Permission("postalservice.mail.send."+newType.getDisplayName().toLowerCase(), PermissionDefault.FALSE);
		permission.addParent("postalservice.mail.send.*", false);
		PostalService.getPlugin().getServer().getPluginManager().addPermission(permission);
	}

	public void deregisterMailType(MailType mailType) {
		mailTypes.remove(mailType);
		PostalService.getPlugin().getServer().getPluginManager().removePermission("postalservice.mail.send." + mailType.getDisplayName().toLowerCase());
	}

	public void deregisterMailTypeByIdentifier(String identifier) {
		MailType typeToRemove = null;
		for(MailType type : mailTypes) {
			if(type.getIdentifier().equalsIgnoreCase(identifier)) {
				typeToRemove = type;
			}
		}
		if(typeToRemove != null) {
			mailTypes.remove(typeToRemove);
			PostalService.getPlugin().getServer().getPluginManager().removePermission("postalservice.mail.send." + typeToRemove.getDisplayName().toLowerCase());
		}
	}

	public String[] getMailTypeNames() {
		String[] mailTypeNames = new String[mailTypes.size()];
		for(int i = 0; i < mailTypes.size(); i++) {
			mailTypeNames[i] = mailTypes.get(i).getDisplayName();
		}
		return mailTypeNames;
	}

	public MailType[] getMailTypes() {
		MailType[] types = new MailType[mailTypes.size()];
		try {
			for(int i = 0; i < mailTypes.size(); i++) {
				types[i] = mailTypes.get(i).getClass().newInstance();
			}
		} catch (Exception e) {
			if(Config.ENABLE_DEBUG) e.printStackTrace();
		}
		return types;
	}

	public String[] getMailTypeIdentifiers() {
		String[] mailTypeNames = new String[mailTypes.size()];
		for(int i = 0; i < mailTypes.size(); i++) {
			mailTypeNames[i] = mailTypes.get(i).getIdentifier();
		}
		return mailTypeNames;
	}

	public MailType getMailTypeByName(String name) {
		for(MailType mailType : mailTypes) {
			if(mailType.getDisplayName().equalsIgnoreCase(name))
				try {
					return mailType.getClass().newInstance();
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG) e.printStackTrace();
				}
		}
		return null;
	}

	public MailType getMailTypeByIdentifier(String identifier) {
		for(MailType mailType : mailTypes) {
			if(mailType.getIdentifier().equalsIgnoreCase(identifier))
				try {
					return mailType.getClass().newInstance();
				} catch (Exception e) {
					if(Config.ENABLE_DEBUG) e.printStackTrace();
				}
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
