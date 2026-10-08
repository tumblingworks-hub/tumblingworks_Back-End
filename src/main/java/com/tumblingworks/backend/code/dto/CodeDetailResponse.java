package com.tumblingworks.backend.code.dto;

import com.tumblingworks.backend.code.entity.CodeDetail;

import java.time.LocalDateTime;

public record CodeDetailResponse(
		String codeId,
		String groupId,
		String detailCode,
		String detailCodeName,
		String description,
		boolean useYn,
		int sortOrder,
		String extra1,
		String extra2,
		String extra3,
		LocalDateTime registeredAt,
		String registeredUserId,
		LocalDateTime updatedAt,
		String updatedUserId
) {

	public static CodeDetailResponse from(CodeDetail detail) {
		return new CodeDetailResponse(
				detail.getCodeId(),
				detail.getGroupId(),
				detail.getDetailCode(),
				detail.getDetailCodeName(),
				detail.getDescription(),
				detail.isUseYn(),
				detail.getSortOrder(),
				detail.getExtra1(),
				detail.getExtra2(),
				detail.getExtra3(),
				detail.getRegisteredAt(),
				detail.getRegisteredUserId(),
				detail.getUpdatedAt(),
				detail.getUpdatedUserId()
		);
	}
}
