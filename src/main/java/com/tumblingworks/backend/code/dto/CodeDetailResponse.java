package com.tumblingworks.backend.code.dto;

import com.tumblingworks.backend.code.entity.CodeDetail;

public record CodeDetailResponse(
		String codeId,
		String groupId,
		String detailCode,
		String detailCodeName
) {

	public static CodeDetailResponse from(CodeDetail detail) {
		return new CodeDetailResponse(
				detail.getCodeId(),
				detail.getGroupId(),
				detail.getDetailCode(),
				detail.getDetailCodeName()
		);
	}
}
