package com.ccl.jersey.codegen;

public class BeanConvertException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7509674126682896613L;

	public BeanConvertException() {
	}

	public BeanConvertException(String message) {
		super(message);
	}

	public BeanConvertException(Throwable cause) {
		super(cause);
	}

	public BeanConvertException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanConvertException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
