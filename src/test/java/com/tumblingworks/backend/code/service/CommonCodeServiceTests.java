package com.tumblingworks.backend.code.service;

import com.tumblingworks.backend.code.dto.CodeDetailResponse;
import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeGroupRequest;
import com.tumblingworks.backend.code.entity.CodeDetail;
import com.tumblingworks.backend.code.entity.CodeGroup;
import com.tumblingworks.backend.code.repository.CodeDetailRepository;
import com.tumblingworks.backend.code.repository.CodeGroupRepository;
import com.tumblingworks.backend.common.exception.BusinessError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommonCodeServiceTests {

	private CodeGroupRepository codeGroupRepository;
	private CodeDetailRepository codeDetailRepository;
	private CommonCodeService commonCodeService;

	@BeforeEach
	void setUp() {
		codeGroupRepository = mock(CodeGroupRepository.class);
		codeDetailRepository = mock(CodeDetailRepository.class);
		commonCodeService = new CommonCodeService(
				codeGroupRepository,
				codeDetailRepository
		);
	}

	@Test
	void rejectsBlankGroupKey() {
		assertBusinessError(
				() -> commonCodeService.createGroup(
						new CreateCodeGroupRequest("   ", "상태", null),
						null
				),
				"GROUP_KEY_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankGroupName() {
		assertBusinessError(
				() -> commonCodeService.createGroup(
						new CreateCodeGroupRequest("STATUS", "", null),
						null
				),
				"GROUP_NAME_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankGroupIdForDetail() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest(" ", "ACTIVE", "사용", null),
						null
				),
				"GROUP_ID_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankDetailCode() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest("CMGRP1", null, "사용", null),
						null
				),
				"DETAIL_CODE_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankDetailCodeName() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest("CMGRP1", "ACTIVE", "  ", null),
						null
				),
				"DETAIL_CODE_NAME_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void listsActiveGroupsOnly() {
		when(codeGroupRepository.findAllByDeletedFlagOrderBySortOrderAscGroupCodeAsc("N"))
				.thenReturn(List.of(group("CMGRP1", "STATUS")));

		List<CodeGroupResponse> groups = commonCodeService.getGroups();

		assertThat(groups).extracting(CodeGroupResponse::groupKey).containsExactly("STATUS");
		verify(codeGroupRepository).findAllByDeletedFlagOrderBySortOrderAscGroupCodeAsc("N");
	}

	@Test
	void returnsNotFoundForMissingOrDeletedGroup() {
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP9", "N"))
				.thenReturn(Optional.empty());

		assertBusinessError(
				() -> commonCodeService.getGroup("CMGRP9"),
				"CODE_GROUP_NOT_FOUND",
				404
		);
	}

	@Test
	void updatesEditableGroupFieldsAndKeepsGroupKey() {
		CodeGroup group = group("CMGRP1", "STATUS");
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(Optional.of(group));
		when(codeGroupRepository.saveAndFlush(group)).thenReturn(group);

		CodeGroupResponse response = commonCodeService.updateGroup(
				"CMGRP1",
				new UpdateCodeGroupRequest("  상태값  ", "회원 상태", false, 3),
			"admin"
		);

		assertThat(response.groupKey()).isEqualTo("STATUS");
		assertThat(response.groupName()).isEqualTo("상태값");
		assertThat(response.description()).isEqualTo("회원 상태");
		assertThat(response.useYn()).isFalse();
		assertThat(response.sortOrder()).isEqualTo(3);
		assertThat(response.updatedUserId()).isEqualTo("admin");
	}

	@Test
	void rejectsGroupUpdateWithoutRequiredFields() {
		assertBusinessError(
				() -> commonCodeService.updateGroup(
						"CMGRP1",
						new UpdateCodeGroupRequest(" ", null, true, 0),
					"admin"
				),
				"GROUP_NAME_REQUIRED",
				400
		);
		assertBusinessError(
				() -> commonCodeService.updateGroup(
						"CMGRP1",
						new UpdateCodeGroupRequest("상태", null, null, 0),
					"admin"
				),
				"USE_YN_REQUIRED",
				400
		);
		assertBusinessError(
				() -> commonCodeService.updateGroup(
						"CMGRP1",
						new UpdateCodeGroupRequest("상태", null, true, null),
					"admin"
				),
				"SORT_ORDER_REQUIRED",
				400
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void refusesToDeleteGroupWithActiveDetails() {
		CodeGroup group = group("CMGRP1", "STATUS");
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(Optional.of(group));
		when(codeDetailRepository.existsByGroup_GroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(true);

		assertBusinessError(
				() -> commonCodeService.deleteGroup("CMGRP1", "admin"),
				"CODE_GROUP_HAS_DETAILS",
				409
		);
		assertThat(group.getDeletedFlag()).isEqualTo("N");
		verify(codeGroupRepository, never()).saveAndFlush(any(CodeGroup.class));
	}

	@Test
	void softDeletesGroupWithoutDetails() {
		CodeGroup group = group("CMGRP1", "STATUS");
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(Optional.of(group));
		when(codeDetailRepository.existsByGroup_GroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(false);

		commonCodeService.deleteGroup("CMGRP1", "admin");

		assertThat(group.getDeletedFlag()).isEqualTo("Y");
		assertThat(group.getUpdatedUserId()).isEqualTo("admin");
		verify(codeGroupRepository).saveAndFlush(group);
	}

	@Test
	void listsDetailsOnlyForActiveGroup() {
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP9", "N"))
				.thenReturn(Optional.empty());

		assertBusinessError(
				() -> commonCodeService.getDetails("CMGRP9"),
				"CODE_GROUP_NOT_FOUND",
				404
		);
		verifyNoInteractions(codeDetailRepository);
	}

	@Test
	void listsActiveDetailsOfGroup() {
		CodeGroup group = group("CMGRP1", "STATUS");
		when(codeGroupRepository.findByGroupIdAndDeletedFlag("CMGRP1", "N"))
				.thenReturn(Optional.of(group));
		when(codeDetailRepository
				.findAllByGroup_GroupIdAndDeletedFlagOrderBySortOrderAscDetailCodeAsc("CMGRP1", "N"))
				.thenReturn(List.of(detail(group, "CMDTL1", "ACTIVE")));

		List<CodeDetailResponse> details = commonCodeService.getDetails("CMGRP1");

		assertThat(details).extracting(CodeDetailResponse::detailCode).containsExactly("ACTIVE");
		assertThat(details).extracting(CodeDetailResponse::groupId).containsExactly("CMGRP1");
	}

	@Test
	void updatesEditableDetailFieldsAndKeepsDetailCode() {
		CodeDetail detail = detail(group("CMGRP1", "STATUS"), "CMDTL1", "ACTIVE");
		when(codeDetailRepository.findByCodeIdAndDeletedFlag("CMDTL1", "N"))
				.thenReturn(Optional.of(detail));
		when(codeDetailRepository.saveAndFlush(detail)).thenReturn(detail);

		CodeDetailResponse response = commonCodeService.updateDetail(
				"CMDTL1",
				new UpdateCodeDetailRequest(" 활성 ", null, true, 1, "a", null, "c"),
			"admin"
		);

		assertThat(response.detailCode()).isEqualTo("ACTIVE");
		assertThat(response.detailCodeName()).isEqualTo("활성");
		assertThat(response.sortOrder()).isEqualTo(1);
		assertThat(response.extra1()).isEqualTo("a");
		assertThat(response.extra2()).isNull();
		assertThat(response.extra3()).isEqualTo("c");
	}

	@Test
	void rejectsDetailUpdateWithBlankName() {
		assertBusinessError(
				() -> commonCodeService.updateDetail(
						"CMDTL1",
						new UpdateCodeDetailRequest("", null, true, 0, null, null, null),
					"admin"
				),
				"DETAIL_CODE_NAME_REQUIRED",
				400
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void returnsNotFoundForMissingOrDeletedDetail() {
		when(codeDetailRepository.findByCodeIdAndDeletedFlag("CMDTL9", "N"))
				.thenReturn(Optional.empty());

		assertBusinessError(
				() -> commonCodeService.deleteDetail("CMDTL9", "admin"),
				"CODE_DETAIL_NOT_FOUND",
				404
		);
	}

	@Test
	void softDeletesDetail() {
		CodeDetail detail = detail(group("CMGRP1", "STATUS"), "CMDTL1", "ACTIVE");
		when(codeDetailRepository.findByCodeIdAndDeletedFlag("CMDTL1", "N"))
				.thenReturn(Optional.of(detail));

		commonCodeService.deleteDetail("CMDTL1", "admin");

		assertThat(detail.getDeletedFlag()).isEqualTo("Y");
		assertThat(detail.getUpdatedUserId()).isEqualTo("admin");
		verify(codeDetailRepository).saveAndFlush(detail);
	}

	@Test
	void recordsTrimmedAdminUsernameAsRegistrantOfNewGroup() {
		when(codeGroupRepository.existsByGroupCode("STATUS")).thenReturn(false);
		when(codeGroupRepository.saveAndFlush(any(CodeGroup.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		CodeGroupResponse response = commonCodeService.createGroup(
				new CreateCodeGroupRequest("STATUS", "상태", null),
				"  admin  "
		);

		assertThat(response.registeredUserId()).isEqualTo("admin");
		assertThat(response.updatedUserId()).isEqualTo("admin");
	}

	@Test
	void leavesRegistrantEmptyWhenAdminUsernameIsBlank() {
		CodeGroup group = group("CMGRP1", "STATUS");
		when(codeDetailRepository.existsByGroup_GroupIdAndDetailCode("CMGRP1", "ACTIVE"))
				.thenReturn(false);
		when(codeGroupRepository.findById("CMGRP1")).thenReturn(Optional.of(group));
		when(codeDetailRepository.saveAndFlush(any(CodeDetail.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		CodeDetailResponse response = commonCodeService.createDetail(
				new CreateCodeDetailRequest("CMGRP1", "ACTIVE", "사용", null),
				"   "
		);

		assertThat(response.registeredUserId()).isNull();
	}

	@Test
	void rejectsAdminUsernameLongerThanColumn() {
		assertBusinessError(
				() -> commonCodeService.deleteDetail("CMDTL1", "a".repeat(101)),
				"ADMIN_USERNAME_TOO_LONG",
				400
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	private static CodeGroup group(String groupId, String groupKey) {
		CodeGroup group = CodeGroup.create(groupKey, "상태", null);
		ReflectionTestUtils.setField(group, "groupId", groupId);
		return group;
	}

	private static CodeDetail detail(CodeGroup group, String codeId, String detailCode) {
		CodeDetail detail = CodeDetail.create(group, detailCode, "사용", null);
		ReflectionTestUtils.setField(detail, "codeId", codeId);
		return detail;
	}

	private void assertBusinessError(Runnable operation, String expectedCode) {
		assertBusinessError(operation, expectedCode, 400);
	}

	private void assertBusinessError(
			Runnable operation,
			String expectedCode,
			int expectedStatus
	) {
		assertThatThrownBy(operation::run)
				.isInstanceOfSatisfying(BusinessError.class, error -> {
					assertThat(error.getCode()).isEqualTo(expectedCode);
					assertThat(error.getStatus().value()).isEqualTo(expectedStatus);
				});
	}
}
