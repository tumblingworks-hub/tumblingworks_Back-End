package com.tumblingworks.backend.code.dto;

import com.tumblingworks.backend.code.entity.CodeGroup;

public record CodeGroupResponse(
		String groupId,
		String groupKey,
		String groupName
) {

	public static CodeGroupResponse from(CodeGroup group) {
		return new CodeGroupResponse(
				group.getGroupId(),
				group.getGroupCode(),
				group.getGroupName()
		);
	}
}
