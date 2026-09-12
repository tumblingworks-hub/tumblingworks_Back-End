package com.tumblingworks.backend.code.service;

import com.tumblingworks.backend.code.dto.CodeDetailResponse;
import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.entity.CodeDetail;
import com.tumblingworks.backend.code.entity.CodeGroup;
import com.tumblingworks.backend.code.repository.CodeDetailRepository;
import com.tumblingworks.backend.code.repository.CodeGroupRepository;
import com.tumblingworks.backend.common.exception.BusinessError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tumblingworks.backend.common.Utils.isBlank;

@Service
public class CommonCodeService {

	private final CodeGroupRepository codeGroupRepository;
	private final CodeDetailRepository codeDetailRepository;

	public CommonCodeService(
			CodeGroupRepository codeGroupRepository,
			CodeDetailRepository codeDetailRepository
	) {
		this.codeGroupRepository = codeGroupRepository;
		this.codeDetailRepository = codeDetailRepository;
	}

	@Transactional
	public CodeGroupResponse createGroup(CreateCodeGroupRequest request) {
		if (request == null || isBlank(request.groupKey())) {
			throw BusinessError.badRequest(
					"GROUP_KEY_REQUIRED",
					"그룹키는 필수값입니다."
			);
		}
		if (isBlank(request.groupName())) {
			throw BusinessError.badRequest(
					"GROUP_NAME_REQUIRED",
					"그룹명은 필수값입니다."
			);
		}

		String groupKey = request.groupKey().trim();
		if (codeGroupRepository.existsByGroupCode(groupKey)) {
			throw BusinessError.conflict(
					"GROUP_KEY_DUPLICATED",
					"이미 등록된 그룹키입니다."
			);
		}

		CodeGroup saved = codeGroupRepository.saveAndFlush(
				CodeGroup.create(
						groupKey,
						request.groupName().trim(),
						request.description()
				)
		);
		return CodeGroupResponse.from(saved);
	}

	@Transactional
	public CodeDetailResponse createDetail(CreateCodeDetailRequest request) {
		if (request == null || isBlank(request.groupId())) {
			throw BusinessError.badRequest(
					"GROUP_ID_REQUIRED",
					"그룹ID는 필수값입니다."
			);
		}
		if (isBlank(request.detailCode())) {
			throw BusinessError.badRequest(
					"DETAIL_CODE_REQUIRED",
					"상세코드는 필수값입니다."
			);
		}
		if (isBlank(request.detailCodeName())) {
			throw BusinessError.badRequest(
					"DETAIL_CODE_NAME_REQUIRED",
					"상세코드명은 필수값입니다."
			);
		}

		String groupId = request.groupId().trim();
		String detailCode = request.detailCode().trim();
		CodeGroup group = codeGroupRepository.findById(groupId)
				.orElseThrow(() -> BusinessError.notFound(
						"CODE_GROUP_NOT_FOUND",
						"등록된 코드 그룹을 찾을 수 없습니다."
				));

		if (codeDetailRepository.existsByGroup_GroupIdAndDetailCode(
				groupId,
				detailCode
		)) {
			throw BusinessError.conflict(
					"DETAIL_CODE_DUPLICATED",
					"해당 그룹에 이미 등록된 상세코드입니다."
			);
		}

		CodeDetail saved = codeDetailRepository.saveAndFlush(
				CodeDetail.create(
						group,
						detailCode,
						request.detailCodeName().trim(),
						request.description()
				)
		);
		return CodeDetailResponse.from(saved);
	}
}
