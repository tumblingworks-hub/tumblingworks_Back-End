package com.tumblingworks.backend.code.service;

import com.tumblingworks.backend.code.dto.CodeDetailResponse;
import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeGroupRequest;
import com.tumblingworks.backend.code.entity.CodeDetail;
import com.tumblingworks.backend.code.entity.CodeGroup;
import com.tumblingworks.backend.code.entity.DeletedFlag;
import com.tumblingworks.backend.code.repository.CodeDetailRepository;
import com.tumblingworks.backend.code.repository.CodeGroupRepository;
import com.tumblingworks.backend.common.exception.BusinessError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tumblingworks.backend.common.Utils.isBlank;

@Service
public class CommonCodeService {

	private static final int ADMIN_USERNAME_MAX_LENGTH = 100;

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
	public CodeGroupResponse createGroup(
			CreateCodeGroupRequest request,
			String adminUsername
	) {
		String actor = normalizeAdminUsername(adminUsername);
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

		CodeGroup group = CodeGroup.create(
				groupKey,
				request.groupName().trim(),
				request.description()
		);
		group.recordRegisteredBy(actor);
		CodeGroup saved = codeGroupRepository.saveAndFlush(group);
		return CodeGroupResponse.from(saved);
	}

	@Transactional
	public CodeDetailResponse createDetail(
			CreateCodeDetailRequest request,
			String adminUsername
	) {
		String actor = normalizeAdminUsername(adminUsername);
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

		CodeDetail detail = CodeDetail.create(
				group,
				detailCode,
				request.detailCodeName().trim(),
				request.description()
		);
		detail.recordRegisteredBy(actor);
		CodeDetail saved = codeDetailRepository.saveAndFlush(detail);
		return CodeDetailResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<CodeGroupResponse> getGroups() {
		return codeGroupRepository
				.findAllByDeletedFlagOrderBySortOrderAscGroupCodeAsc(DeletedFlag.ACTIVE)
				.stream()
				.map(CodeGroupResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CodeGroupResponse getGroup(String groupId) {
		return CodeGroupResponse.from(findActiveGroup(groupId));
	}

	@Transactional
	public CodeGroupResponse updateGroup(
			String groupId,
			UpdateCodeGroupRequest request,
			String adminUsername
	) {
		String actor = normalizeAdminUsername(adminUsername);
		if (request == null || isBlank(request.groupName())) {
			throw BusinessError.badRequest(
					"GROUP_NAME_REQUIRED",
					"그룹명은 필수값입니다."
			);
		}
		requireUseYnAndSortOrder(request.useYn(), request.sortOrder());

		CodeGroup group = findActiveGroup(groupId);
		group.update(
				request.groupName().trim(),
				request.description(),
				request.useYn(),
				request.sortOrder(),
				actor
		);
		return CodeGroupResponse.from(codeGroupRepository.saveAndFlush(group));
	}

	@Transactional
	public void deleteGroup(String groupId, String adminUsername) {
		String actor = normalizeAdminUsername(adminUsername);
		CodeGroup group = findActiveGroup(groupId);
		if (codeDetailRepository.existsByGroup_GroupIdAndDeletedFlag(
				group.getGroupId(),
				DeletedFlag.ACTIVE
		)) {
			throw BusinessError.conflict(
					"CODE_GROUP_HAS_DETAILS",
					"상세코드가 남아 있는 그룹은 삭제할 수 없습니다. 상세코드를 먼저 삭제하세요."
			);
		}
		group.markDeleted(actor);
		codeGroupRepository.saveAndFlush(group);
	}

	@Transactional(readOnly = true)
	public List<CodeDetailResponse> getDetails(String groupId) {
		CodeGroup group = findActiveGroup(groupId);
		return codeDetailRepository
				.findAllByGroup_GroupIdAndDeletedFlagOrderBySortOrderAscDetailCodeAsc(
						group.getGroupId(),
						DeletedFlag.ACTIVE
				)
				.stream()
				.map(CodeDetailResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CodeDetailResponse getDetail(String codeId) {
		return CodeDetailResponse.from(findActiveDetail(codeId));
	}

	@Transactional
	public CodeDetailResponse updateDetail(
			String codeId,
			UpdateCodeDetailRequest request,
			String adminUsername
	) {
		String actor = normalizeAdminUsername(adminUsername);
		if (request == null || isBlank(request.detailCodeName())) {
			throw BusinessError.badRequest(
					"DETAIL_CODE_NAME_REQUIRED",
					"상세코드명은 필수값입니다."
			);
		}
		requireUseYnAndSortOrder(request.useYn(), request.sortOrder());

		CodeDetail detail = findActiveDetail(codeId);
		detail.update(
				request.detailCodeName().trim(),
				request.description(),
				request.useYn(),
				request.sortOrder(),
				request.extra1(),
				request.extra2(),
				request.extra3(),
				actor
		);
		return CodeDetailResponse.from(codeDetailRepository.saveAndFlush(detail));
	}

	@Transactional
	public void deleteDetail(String codeId, String adminUsername) {
		String actor = normalizeAdminUsername(adminUsername);
		CodeDetail detail = findActiveDetail(codeId);
		detail.markDeleted(actor);
		codeDetailRepository.saveAndFlush(detail);
	}

	private CodeGroup findActiveGroup(String groupId) {
		return codeGroupRepository
				.findByGroupIdAndDeletedFlag(groupId, DeletedFlag.ACTIVE)
				.orElseThrow(() -> BusinessError.notFound(
						"CODE_GROUP_NOT_FOUND",
						"등록된 코드 그룹을 찾을 수 없습니다."
				));
	}

	private CodeDetail findActiveDetail(String codeId) {
		return codeDetailRepository
				.findByCodeIdAndDeletedFlag(codeId, DeletedFlag.ACTIVE)
				.orElseThrow(() -> BusinessError.notFound(
						"CODE_DETAIL_NOT_FOUND",
						"등록된 상세코드를 찾을 수 없습니다."
				));
	}

	/**
	 * X-Admin-Username 헤더 값을 regist_user_id / update_user_id 에 넣을 형태로 만든다.
	 * 헤더가 없거나 비어 있으면 null(누가 바꿨는지 모름). 컬럼 길이(100)를 넘으면 거부한다.
	 * core backend에는 인증이 없어 이 값은 호출자가 주장하는 이름일 뿐이다.
	 */
	private String normalizeAdminUsername(String adminUsername) {
		if (isBlank(adminUsername)) {
			return null;
		}
		String trimmed = adminUsername.trim();
		if (trimmed.length() > ADMIN_USERNAME_MAX_LENGTH) {
			throw BusinessError.badRequest(
					"ADMIN_USERNAME_TOO_LONG",
					"변경자 이름은 " + ADMIN_USERNAME_MAX_LENGTH + "자를 넘을 수 없습니다."
			);
		}
		return trimmed;
	}

	private void requireUseYnAndSortOrder(Boolean useYn, Integer sortOrder) {
		if (useYn == null) {
			throw BusinessError.badRequest(
					"USE_YN_REQUIRED",
					"사용여부는 필수값입니다."
			);
		}
		if (sortOrder == null) {
			throw BusinessError.badRequest(
					"SORT_ORDER_REQUIRED",
					"정렬순서는 필수값입니다."
			);
		}
	}
}
