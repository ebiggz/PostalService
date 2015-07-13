package com.gmail.erikbigler.postalservice.mail;

import java.util.Date;


public class Mail {

	private int mailID;
	private String sender;
	private String recipient;
	private String message;
	private String attachmentData;
	private MailType type;
	private Date time;
	private MailStatus status;

	public Mail(int mailID, String sender, String recipient, String message, String attachmentData, MailType type, Date time, MailStatus status) {
		this.mailID = mailID;
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.time = time;
		this.status = status;
		this.attachmentData = attachmentData;
		if(type == null) return;
		this.type = type;
		if(hasAttachments()) {
			type.loadAttachments(attachmentData);
		}
	}

	public enum MailStatus {
		UNREAD, READ, CLAIMED
	}

	public int getMailID() {
		return mailID;
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
