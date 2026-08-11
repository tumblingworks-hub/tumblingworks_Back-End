package com.tumblingworks.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Test", description = "Application availability test API")
@RestController
@RequestMapping("/api/test")
public class TestController {

	@Operation(summary = "Test the API", description = "Returns a response showing that the server is running.")
	@GetMapping
	public TestResponse test() {
		return new TestResponse("Tumblingworks Back-End is running", Instant.now());
	}

	public record TestResponse(String message, Instant timestamp) {
	}
}
