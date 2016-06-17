package com.ticketapp.service.exception;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2750243857813115240L;

	public NotFoundException(String message) {
		super(message);
	}
}
