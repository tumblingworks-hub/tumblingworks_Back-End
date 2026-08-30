package com.tumblingworks.backend.errorlog.entity;

import com.tumblingworks.backend.errorlog.dto.ErrorLogCreateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "error_log")
public class ErrorLog {

	@Id
	@ErrorLogGeneratedId
	@Column(name = "id", nullable = false, updatable = false)
	private String id;

	@Column(name = "request_id", length = 36)
	private String requestId;

	@Column(name = "occurred_at", nullable = false)
	private OffsetDateTime occurredAt;

	@Column(name = "service_name", nullable = false, length = 100)
	private String serviceName;

	@Column(name = "environment", nullable = false, length = 30)
	private String environment;

	@Column(name = "http_method", length = 10)
	private String httpMethod;

	@Column(name = "request_uri", length = 2000)
	private String requestUri;

	@Column(name = "http_status")
	private Integer httpStatus;

	@Column(name = "error_code", length = 100)
	private String errorCode;

	@Column(name = "exception_class", length = 500)
	private String exceptionClass;

	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;

	@Column(name = "stack_trace", columnDefinition = "text")
	private String stackTrace;

	@Column(name = "user_id", length = 100)
	private String userId;

	@Column(name = "client_ip", length = 100)
	private String clientIp;

	@Column(name = "user_agent", length = 1000)
	private String userAgent;

	@Column(name = "resolved", nullable = false)
	private boolean resolved;

	protected ErrorLog() {
	}

	private ErrorLog(ErrorLogCreateRequest request) {
		this.requestId = request.requestId();
		this.occurredAt = OffsetDateTime.now();
		this.serviceName = request.serviceName();
		this.environment = request.environment();
		this.httpMethod = request.httpMethod();
		this.requestUri = request.requestUri();
		this.httpStatus = request.httpStatus();
		this.errorCode = request.errorCode();
		this.exceptionClass = request.exceptionClass();
		this.errorMessage = request.errorMessage();
		this.stackTrace = request.stackTrace();
		this.userId = request.userId();
		this.clientIp = request.clientIp();
		this.userAgent = request.userAgent();
		this.resolved = false;
	}

	public static ErrorLog create(ErrorLogCreateRequest request) {
		return new ErrorLog(request);
	}

	public String getId() {
		return id;
	}
}
