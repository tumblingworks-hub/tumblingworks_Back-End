package com.tumblingworks.backend.errorlog.dto;

public record ErrorLogCreateRequest(
		String requestId,
		String serviceName,
		String environment,
		String httpMethod,
		String requestUri,
		Integer httpStatus,
		String errorCode,
		String exceptionClass,
		String errorMessage,
		String stackTrace,
		String userId,
		String clientIp,
		String userAgent
) {
}
