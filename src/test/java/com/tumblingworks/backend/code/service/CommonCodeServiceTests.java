package com.tumblingworks.backend.code.service;

import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.repository.CodeDetailRepository;
import com.tumblingworks.backend.code.repository.CodeGroupRepository;
import com.tumblingworks.backend.common.exception.BusinessError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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
						new CreateCodeGroupRequest("   ", "상태", null)
				),
				"GROUP_KEY_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankGroupName() {
		assertBusinessError(
				() -> commonCodeService.createGroup(
						new CreateCodeGroupRequest("STATUS", "", null)
				),
				"GROUP_NAME_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankGroupIdForDetail() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest(" ", "ACTIVE", "사용", null)
				),
				"GROUP_ID_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankDetailCode() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest("CMGRP1", null, "사용", null)
				),
				"DETAIL_CODE_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	@Test
	void rejectsBlankDetailCodeName() {
		assertBusinessError(
				() -> commonCodeService.createDetail(
						new CreateCodeDetailRequest("CMGRP1", "ACTIVE", "  ", null)
				),
				"DETAIL_CODE_NAME_REQUIRED"
		);
		verifyNoInteractions(codeGroupRepository, codeDetailRepository);
	}

	private void assertBusinessError(Runnable operation, String expectedCode) {
		assertThatThrownBy(operation::run)
				.isInstanceOfSatisfying(BusinessError.class, error -> {
					assertThat(error.getCode()).isEqualTo(expectedCode);
					assertThat(error.getStatus().value()).isEqualTo(400);
				});
	}
}
