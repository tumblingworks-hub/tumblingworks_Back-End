package com.tumblingworks.backend.code.dto;

public record CreateCodeGroupRequest(
		String groupKey,
		String groupName,
		String description
) {
}
