package com.tumblingworks.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RequestLifecycleInterceptor implements HandlerInterceptor {

	private static final Logger log =
			LoggerFactory.getLogger(RequestLifecycleInterceptor.class);
	private static final String REQUEST_ID_ATTRIBUTE =
			RequestLifecycleInterceptor.class.getName() + ".requestId";
	private static final String START_TIME_ATTRIBUTE =
			RequestLifecycleInterceptor.class.getName() + ".startTime";
	private static final String REQUEST_ID_HEADER = "X-Request-ID";

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler
	) {
		String requestId = UUID.randomUUID().toString();

		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
		response.setHeader(REQUEST_ID_HEADER, requestId);

		log.info(
				"[REQUEST START] id={} method={} uri={}",
				requestId,
				request.getMethod(),
				request.getRequestURI()
		);

		return true;
	}

	@Override
	public void postHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			ModelAndView modelAndView
	) {
		log.info(
				"[REQUEST SUCCESS] id={} method={} uri={} status={} durationMs={}",
				requestId(request),
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				durationMillis(request)
		);
	}

	@Override
	public void afterCompletion(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception exception
	) {
		if (exception != null) {
			log.error(
					"[REQUEST ERROR] id={} method={} uri={} status={} durationMs={}",
					requestId(request),
					request.getMethod(),
					request.getRequestURI(),
					response.getStatus(),
					durationMillis(request),
					exception
			);
		} else if (response.getStatus() >= HttpServletResponse.SC_BAD_REQUEST) {
			log.warn(
					"[REQUEST ERROR] id={} method={} uri={} status={} durationMs={}",
					requestId(request),
					request.getMethod(),
					request.getRequestURI(),
					response.getStatus(),
					durationMillis(request)
			);
		}
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute(REQUEST_ID_ATTRIBUTE);
		return requestId == null ? "unknown" : requestId.toString();
	}

	private long durationMillis(HttpServletRequest request) {
		Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
		if (!(startTime instanceof Long startTimeNanos)) {
			return 0;
		}

		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
	}
}
