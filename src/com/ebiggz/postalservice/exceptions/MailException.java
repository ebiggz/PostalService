package com.ebiggz.postalservice.exceptions;


public class MailException extends Exception {

	private static final long serialVersionUID = 1L;

	private String errorMessage;

	public MailException(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
