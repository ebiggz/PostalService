package com.gmail.erikbigler.postalservice.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.gmail.erikbigler.postalservice.config.Config;

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

	public Mail(long mailID, long receivedID, String sender, String recipient, String message, String attachmentData, MailType type, Date time, MailStatus status) {
		this.mailID = mailID;
		this.receivedID = receivedID;
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.time = time;
		this.status = status;
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

	public String getTimeString() {
		SimpleDateFormat sdf = new SimpleDateFormat(Config.DATE_FORMAT, Locale.forLanguageTag(Config.LOCALE_TAG));
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
}
