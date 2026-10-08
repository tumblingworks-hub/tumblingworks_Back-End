package com.tumblingworks.backend.code.dto;

public record UpdateCodeGroupRequest(
		String groupName,
		String description,
		Boolean useYn,
		Integer sortOrder
) {
}
