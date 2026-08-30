package com.tumblingworks.backend.errorlog.service;

import com.tumblingworks.backend.errorlog.dto.ErrorLogCreateRequest;
import com.tumblingworks.backend.errorlog.entity.ErrorLog;
import com.tumblingworks.backend.errorlog.repository.ErrorLogRepository;
import com.tumblingworks.backend.interceptor.RequestLifecycleInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Service
public class ErrorLogService {

	public static final String ERROR_LOGGED_ATTRIBUTE =
			ErrorLogService.class.getName() + ".logged";
	private static final Logger log = LoggerFactory.getLogger(ErrorLogService.class);
	private static final int MAX_STACK_TRACE_LENGTH = 20_000;

	private final ErrorLogRepository errorLogRepository;
	private final TransactionTemplate transactionTemplate;
	private final String serviceName;
	private final String environment;

	public ErrorLogService(
			ErrorLogRepository errorLogRepository,
			PlatformTransactionManager transactionManager,
			@Value("${app.service-name}") String serviceName,
			@Value("${app.environment}") String environment
	) {
		this.errorLogRepository = errorLogRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(
				TransactionDefinition.PROPAGATION_REQUIRES_NEW
		);
		this.serviceName = serviceName;
		this.environment = environment;
	}

	public void saveSafely(
			HttpServletRequest request,
			int httpStatus,
			String errorCode,
			Throwable exception
	) {
		if (Boolean.TRUE.equals(request.getAttribute(ERROR_LOGGED_ATTRIBUTE))) {
			return;
		}

		request.setAttribute(ERROR_LOGGED_ATTRIBUTE, true);
		ErrorLogCreateRequest errorLog = createRequest(
				request,
				httpStatus,
				errorCode,
				exception
		);

		try {
			transactionTemplate.executeWithoutResult(status ->
					errorLogRepository.saveAndFlush(ErrorLog.create(errorLog))
			);
		} catch (Exception saveException) {
			log.error(
					"Failed to persist error log. requestId={}",
					errorLog.requestId(),
					saveException
			);
		}
	}

	private ErrorLogCreateRequest createRequest(
			HttpServletRequest request,
			int httpStatus,
			String errorCode,
			Throwable exception
	) {
		Object requestIdAttribute = request.getAttribute(
				RequestLifecycleInterceptor.REQUEST_ID_ATTRIBUTE
		);
		String requestId = requestIdAttribute == null
				? UUID.randomUUID().toString()
				: requestIdAttribute.toString();
		String exceptionClass = exception == null
				? null
				: truncate(exception.getClass().getName(), 500);
		String errorMessage = exception == null
				? "HTTP request completed with error status " + httpStatus
				: exception.getMessage();

		return new ErrorLogCreateRequest(
				requestId,
				serviceName,
				environment,
				truncate(request.getMethod(), 10),
				truncate(request.getRequestURI(), 2000),
				httpStatus,
				truncate(errorCode, 100),
				exceptionClass,
				errorMessage,
				stackTrace(exception),
				request.getUserPrincipal() == null
						? null
						: truncate(request.getUserPrincipal().getName(), 100),
				truncate(request.getRemoteAddr(), 100),
				truncate(request.getHeader("User-Agent"), 1000)
		);
	}

	private String stackTrace(Throwable exception) {
		if (exception == null) {
			return null;
		}

		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		return truncate(writer.toString(), MAX_STACK_TRACE_LENGTH);
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
