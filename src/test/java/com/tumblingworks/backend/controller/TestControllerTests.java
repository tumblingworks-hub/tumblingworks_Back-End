package com.tumblingworks.backend.controller;

import com.tumblingworks.backend.config.WebConfig;
import com.tumblingworks.backend.errorlog.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@WebMvcTest(TestController.class)
@Import(WebConfig.class)
class TestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ErrorLogService errorLogService;

	@Test
	void returnsServerStatus() throws Exception {
		mockMvc.perform(get("/api/test"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Tumblingworks Back-End is running"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void allowsRequestsFromMakersLog() throws Exception {
		mockMvc.perform(get("/api/test").header("Origin", "http://localhost:3000"))
				.andExpect(status().isOk())
				.andExpect(header().string(
						"Access-Control-Allow-Origin",
						"http://localhost:3000"
				));
	}

	@Test
	void returnsTrueForBlankValueUsingKotlinUtility() throws Exception {
		mockMvc.perform(get("/api/test/blank").param("value", "   "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.value").value("   "))
				.andExpect(jsonPath("$.blank").value(true));
	}

	@Test
	void returnsFalseForNonBlankValueUsingKotlinUtility() throws Exception {
		mockMvc.perform(get("/api/test/blank").param("value", "tumblingworks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.value").value("tumblingworks"))
				.andExpect(jsonPath("$.blank").value(false));
	}

	@Test
	void handlesIntentionalErrorAndRequestsErrorLogPersistence() throws Exception {
		mockMvc.perform(get("/api/test/error"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
				.andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
				.andExpect(jsonPath("$.path").value("/api/test/error"))
				.andExpect(jsonPath("$.timestamp").exists());

		verify(errorLogService).saveSafely(
				any(HttpServletRequest.class),
				eq(500),
				eq("INTERNAL_SERVER_ERROR"),
				any(IllegalStateException.class)
		);
	}
}
