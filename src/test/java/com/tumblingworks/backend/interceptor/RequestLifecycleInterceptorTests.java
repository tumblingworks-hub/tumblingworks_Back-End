package com.tumblingworks.backend.interceptor;

import com.tumblingworks.backend.errorlog.service.ErrorLogService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class RequestLifecycleInterceptorTests {

	private final RequestLifecycleInterceptor interceptor =
			new RequestLifecycleInterceptor(mock(ErrorLogService.class));

	@Test
	void startsRequestAndReturnsRequestIdHeader() {
		MockHttpServletRequest request =
				new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean shouldContinue = interceptor.preHandle(request, response, new Object());

		assertThat(shouldContinue).isTrue();
		assertThat(response.getHeader("X-Request-ID")).isNotBlank();
	}

	@Test
	void handlesSuccessfulCompletion() {
		MockHttpServletRequest request =
				new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Object handler = new Object();

		interceptor.preHandle(request, response, handler);

		assertThatCode(() -> {
			interceptor.postHandle(request, response, handler, null);
			interceptor.afterCompletion(request, response, handler, null);
		}).doesNotThrowAnyException();
	}

	@Test
	void handlesExceptionalCompletion() {
		MockHttpServletRequest request =
				new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Object handler = new Object();
		RuntimeException exception = new RuntimeException("test failure");

		interceptor.preHandle(request, response, handler);
		response.setStatus(500);

		assertThatCode(() ->
				interceptor.afterCompletion(request, response, handler, exception)
		).doesNotThrowAnyException();
	}
}
