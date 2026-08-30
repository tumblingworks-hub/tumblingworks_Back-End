package com.tumblingworks.backend.common.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
		String code,
		String message,
		String requestId,
		String path,
		OffsetDateTime timestamp
) {
}
