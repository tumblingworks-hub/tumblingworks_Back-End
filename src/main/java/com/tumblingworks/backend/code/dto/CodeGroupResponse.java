package com.tumblingworks.backend.code.dto;

import com.tumblingworks.backend.code.entity.CodeGroup;

import java.time.LocalDateTime;

public record CodeGroupResponse(
		String groupId,
		String groupKey,
		String groupName,
		String description,
		boolean useYn,
		int sortOrder,
		LocalDateTime registeredAt,
		LocalDateTime updatedAt
) {

	public static CodeGroupResponse from(CodeGroup group) {
		return new CodeGroupResponse(
				group.getGroupId(),
				group.getGroupCode(),
				group.getGroupName(),
				group.getDescription(),
				group.isUseYn(),
				group.getSortOrder(),
				group.getRegisteredAt(),
				group.getUpdatedAt()
		);
	}
}
