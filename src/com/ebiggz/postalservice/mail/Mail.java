package com.ebiggz.postalservice.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.ebiggz.postalservice.config.Config;
import com.ebiggz.postalservice.config.WorldGroup;

public class Mail {

	private long mailID;
	private long receivedID;
	private String sender;
	private String recipient;
	private String message;
	private String attachmentData;
	private MailType type;
	private Date time;
	private MailStatus status;
	private WorldGroup worldGroup;

	public Mail(long mailID, long receivedID, String sender, String recipient, String message, String attachmentData, MailType type, Date time, MailStatus status, WorldGroup worldGroup) {
		this.mailID = mailID;
		this.receivedID = receivedID;
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.time = time;
		this.status = status;
		this.worldGroup = worldGroup;
		if(attachmentData != null && !attachmentData.equalsIgnoreCase("null")) {
			this.attachmentData = attachmentData;
		}
		if (type == null)
			return;
		this.type = type;
		if (hasAttachments()) {
			type.loadAttachments(attachmentData);
		}
	}

	public enum MailStatus {
		UNREAD, READ, CLAIMED
	}

	public long getMailID() {
		return mailID;
	}

	public long getReceivedID() {
		return receivedID;
	}

	public String getMessage() {
		return message;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public Date getTime() {
		return time;
	}

	public String getTimeString(String timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(Config.DATE_FORMAT, Locale.forLanguageTag(Config.LOCALE_TAG));
		if(timeZone != null && !timeZone.equalsIgnoreCase("null") && !timeZone.isEmpty()) {
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		} else {
			sdf.setTimeZone(TimeZone.getTimeZone(Config.DEFAULT_TIMEZONE));
		}
		return sdf.format(time);
	}

	public boolean isClaimed() {
		return (status == MailStatus.CLAIMED);
	}

	public boolean isRead() {
		return (status != MailStatus.UNREAD);
	}

	public boolean hasAttachments() {
		return (attachmentData != null && !attachmentData.isEmpty());
	}

	public MailStatus getStatus() {
		return status;
	}

	public MailType getType() {
		return type;
	}

	public WorldGroup getWorldGroup() {
		return worldGroup;
	}
}
