package com.tumblingworks.backend.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessError extends RuntimeException {

	private final String code;
	private final HttpStatus status;

	public BusinessError(String code, String message, HttpStatus status) {
		super(message);
		this.code = code;
		this.status = status;
	}

	public static BusinessError badRequest(String code, String message) {
		return new BusinessError(code, message, HttpStatus.BAD_REQUEST);
	}

	public static BusinessError conflict(String code, String message) {
		return new BusinessError(code, message, HttpStatus.CONFLICT);
	}

	public static BusinessError notFound(String code, String message) {
		return new BusinessError(code, message, HttpStatus.NOT_FOUND);
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
