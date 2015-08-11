package com.ebiggz.postalservice.exceptions;


public class MailboxException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Reason reason;

	public MailboxException(Reason reason) {
		this.reason = reason;
	}

	public enum Reason {
		ALREADY_EXISTS, MAX_REACHED, NO_PERMISSION, NOT_CHEST, DOESNT_EXIST, NOT_OWNER, DOUBLE_CHEST, UNKOWN
	}

	public Reason getReason() {
		return reason;
	}

}
