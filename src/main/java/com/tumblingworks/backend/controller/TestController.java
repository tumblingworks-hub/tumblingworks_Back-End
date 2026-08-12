package com.tumblingworks.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static com.tumblingworks.backend.common.Utils.isBlank;

@Tag(name = "Test", description = "Application availability test API")
@RestController
@RequestMapping("/api/test")
public class TestController {

	@Operation(summary = "Test the API", description = "Returns a response showing that the server is running.")
	@GetMapping
	public TestResponse test() {
		return new TestResponse("Tumblingworks Back-End is running", Instant.now());
	}

	@Operation(
			summary = "Check whether a value is blank",
			description = "Returns true when the value is missing, empty, or contains only whitespace."
	)
	@GetMapping("/blank")
	public BlankCheckResponse checkBlank(@RequestParam(required = false) String value) {
		return new BlankCheckResponse(value, isBlank(value));
	}

	public record TestResponse(String message, Instant timestamp) {
	}

	public record BlankCheckResponse(String value, boolean blank) {
	}
}
