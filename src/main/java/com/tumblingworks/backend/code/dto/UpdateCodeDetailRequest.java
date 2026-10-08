package com.tumblingworks.backend.code.dto;

public record UpdateCodeDetailRequest(
		String detailCodeName,
		String description,
		Boolean useYn,
		Integer sortOrder,
		String extra1,
		String extra2,
		String extra3
) {
}
