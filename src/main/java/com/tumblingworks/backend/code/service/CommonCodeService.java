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
			UpdateCodeGroupRequest request
	) {
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
				request.sortOrder()
		);
		return CodeGroupResponse.from(codeGroupRepository.saveAndFlush(group));
	}

	@Transactional
	public void deleteGroup(String groupId) {
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
		group.markDeleted();
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
			UpdateCodeDetailRequest request
	) {
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
				request.extra3()
		);
		return CodeDetailResponse.from(codeDetailRepository.saveAndFlush(detail));
	}

	@Transactional
	public void deleteDetail(String codeId) {
		CodeDetail detail = findActiveDetail(codeId);
		detail.markDeleted();
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
