package com.tumblingworks.backend.code.controller;

import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.UpdateCodeGroupRequest;
import com.tumblingworks.backend.code.service.CommonCodeService;
import com.tumblingworks.backend.common.exception.BusinessError;
import com.tumblingworks.backend.config.WebConfig;
import com.tumblingworks.backend.errorlog.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonCodeController.class)
@Import(WebConfig.class)
class CommonCodeControllerTests {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 7, 12, 0);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CommonCodeService commonCodeService;

	@MockitoBean
	private ErrorLogService errorLogService;

	@Test
	void listsCodeGroups() throws Exception {
		when(commonCodeService.getGroups()).thenReturn(List.of(groupResponse()));

		mockMvc.perform(get("/api/codes/groups"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].groupId").value("CMGRP1"))
				.andExpect(jsonPath("$[0].groupKey").value("STATUS"))
				.andExpect(jsonPath("$[0].useYn").value(true))
				.andExpect(jsonPath("$[0].sortOrder").value(0));
	}

	@Test
	void updatesCodeGroup() throws Exception {
		when(commonCodeService.updateGroup(
				eq("CMGRP1"),
				eq(new UpdateCodeGroupRequest("상태", "설명", true, 2))
		)).thenReturn(groupResponse());

		mockMvc.perform(put("/api/codes/groups/CMGRP1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"groupName":"상태","description":"설명","useYn":true,"sortOrder":2}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.groupId").value("CMGRP1"));
	}

	@Test
	void returnsNotFoundAndRequestsErrorLogForMissingGroup() throws Exception {
		BusinessError error = BusinessError.notFound(
				"CODE_GROUP_NOT_FOUND",
				"등록된 코드 그룹을 찾을 수 없습니다."
		);
		when(commonCodeService.getGroup("CMGRP9")).thenThrow(error);

		mockMvc.perform(get("/api/codes/groups/CMGRP9"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CODE_GROUP_NOT_FOUND"))
				.andExpect(jsonPath("$.path").value("/api/codes/groups/CMGRP9"))
				.andExpect(jsonPath("$.timestamp").exists());

		verify(errorLogService).saveSafely(
				any(HttpServletRequest.class),
				eq(404),
				eq("CODE_GROUP_NOT_FOUND"),
				any(BusinessError.class)
		);
	}

	@Test
	void returnsConflictWhenDeletingGroupWithDetails() throws Exception {
		doThrow(BusinessError.conflict(
				"CODE_GROUP_HAS_DETAILS",
				"상세코드가 남아 있는 그룹은 삭제할 수 없습니다. 상세코드를 먼저 삭제하세요."
		)).when(commonCodeService).deleteGroup("CMGRP1");

		mockMvc.perform(delete("/api/codes/groups/CMGRP1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CODE_GROUP_HAS_DETAILS"));
	}

	@Test
	void deletesCodeDetailWithNoContent() throws Exception {
		mockMvc.perform(delete("/api/codes/details/CMDTL1"))
				.andExpect(status().isNoContent());

		verify(commonCodeService).deleteDetail("CMDTL1");
	}

	@Test
	void allowsRequestsFromMakersLog() throws Exception {
		when(commonCodeService.getGroups()).thenReturn(List.of());

		mockMvc.perform(get("/api/codes/groups").header("Origin", "http://localhost:3000"))
				.andExpect(status().isOk())
				.andExpect(header().string(
						"Access-Control-Allow-Origin",
						"http://localhost:3000"
				));
	}

	private static CodeGroupResponse groupResponse() {
		return new CodeGroupResponse(
				"CMGRP1",
				"STATUS",
				"상태",
				null,
				true,
				0,
				NOW,
				NOW
		);
	}
}
