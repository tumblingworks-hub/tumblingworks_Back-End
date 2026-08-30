package com.tumblingworks.backend.common.exception;

import com.tumblingworks.backend.errorlog.service.ErrorLogService;
import com.tumblingworks.backend.interceptor.RequestLifecycleInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final ErrorLogService errorLogService;

	public GlobalExceptionHandler(ErrorLogService errorLogService) {
		this.errorLogService = errorLogService;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(
				request,
				exception,
				HttpStatus.BAD_REQUEST,
				"VALIDATION_ERROR",
				message
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(
			Exception exception,
			HttpServletRequest request
	) {
		return createErrorResponse(
				request,
				exception,
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_SERVER_ERROR",
				"서버 오류가 발생했습니다."
		);
	}

	private ResponseEntity<ErrorResponse> createErrorResponse(
			HttpServletRequest request,
			Exception exception,
			HttpStatus status,
			String errorCode,
			String message
	) {
		errorLogService.saveSafely(
				request,
				status.value(),
				errorCode,
				exception
		);

		Object requestId = request.getAttribute(
				RequestLifecycleInterceptor.REQUEST_ID_ATTRIBUTE
		);
		ErrorResponse response = new ErrorResponse(
				errorCode,
				message,
				requestId == null ? null : requestId.toString(),
				request.getRequestURI(),
				OffsetDateTime.now()
		);

		return ResponseEntity.status(status).body(response);
	}
}
