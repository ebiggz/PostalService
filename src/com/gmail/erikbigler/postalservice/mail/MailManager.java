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
	private List<MailType> mailTypesStorage = new ArrayList<MailType>();
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

	public void loadEnabledMailTypes() {
		deregisterAllMailTypes();
		mailTypes.clear();
		for(MailType newType : mailTypesStorage) {
			if(Config.mailTypeIsDisabled(newType)) continue;
			mailTypes.add(newType);
		}
	}

	public void clearMailTypes() {
		mailTypesStorage.clear();
		mailTypes.clear();
	}

	public void deregisterAllMailTypes() {
		//PostalService.getPlugin().getServer().getPluginManager().removePermission(PostalService.getPlugin().getServer().getPluginManager().getPermission("postalservice.mail.send.*"));
		for(int i = 0; i < mailTypes.size(); i++) {
			MailType type = mailTypes.get(i);
			deregisterMailType(type);
			mailTypes.remove(type);
		}
		//PostalService.getPlugin().getServer().getPluginManager().addPermission(new Permission("postalservice.mail.send.*", PermissionDefault.FALSE));
	}

	public void registerMailType(MailType newType) {
		for(MailType mailType : mailTypesStorage) {
			if(mailType.getDisplayName().equalsIgnoreCase(newType.getDisplayName()) || mailType.getIdentifier().equalsIgnoreCase(newType.getIdentifier())) {
				PostalService.getPlugin().getLogger().warning("A plugin attempted to register a mail type named " + newType.getDisplayName() + " with the identifier " + newType.getIdentifier() + " but a mail type with that name or identifier already exists!");
				return;
			}
		}
		mailTypesStorage.add(newType);
		Permission typePerm = new Permission("postalservice.mail.send."+newType.getDisplayName().toLowerCase().trim(), PermissionDefault.FALSE);
		typePerm.addParent("postalservice.mail.send.*", true);
		PostalService.getPlugin().getServer().getPluginManager().addPermission(typePerm);
		if(!Config.mailTypeIsDisabled(newType)) {
			mailTypes.add(newType);
		}
	}

	public void deregisterMailType(MailType mailType) {
		//PostalService.getPlugin().getServer().getPluginManager().removePermission("postalservice.mail.send." + mailType.getDisplayName().toLowerCase());
		mailTypes.remove(mailType);
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

	public void deregisterMailTypeByName(String name) {
		MailType typeToRemove = null;
		for(MailType type : mailTypes) {
			if(type.getDisplayName().equalsIgnoreCase(name)) {
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
