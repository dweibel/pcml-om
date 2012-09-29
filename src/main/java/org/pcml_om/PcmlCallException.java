package org.pcml_om;

public class PcmlCallException extends Exception {

	private static final long serialVersionUID = 1L;

	public PcmlCallException() {
	}

	public PcmlCallException(String message) {
		super(message);
	}

	public PcmlCallException(Throwable cause) {
		super(cause);
	}

	public PcmlCallException(String message, Throwable cause) {
		super(message, cause);
	}

}
