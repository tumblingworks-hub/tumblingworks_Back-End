package com.tumblingworks.backend.code.dto;

public record CreateCodeDetailRequest(
		String groupId,
		String detailCode,
		String detailCodeName,
		String description
) {
}
