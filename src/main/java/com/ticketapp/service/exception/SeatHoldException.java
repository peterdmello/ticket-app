package com.ticketapp.service.exception;

public class SeatHoldException extends RuntimeException {

	private static final long serialVersionUID = 5710480383575137674L;

	public SeatHoldException(String message) {
		super(message);
	}

	public SeatHoldException(String message, Throwable cause) {
		super(message, cause);
	}
}
