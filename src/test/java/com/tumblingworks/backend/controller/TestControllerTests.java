package com.tumblingworks.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
class TestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsServerStatus() throws Exception {
		mockMvc.perform(get("/api/test"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Tumblingworks Back-End is running"))
				.andExpect(jsonPath("$.timestamp").exists());
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
}
