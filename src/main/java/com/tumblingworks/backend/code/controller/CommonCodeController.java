package com.tumblingworks.backend.code.controller;

import com.tumblingworks.backend.code.dto.CodeDetailResponse;
import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.service.CommonCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common Code", description = "Common code group and detail management API")
@RestController
@RequestMapping("/api/codes")
public class CommonCodeController {

	private final CommonCodeService commonCodeService;

	public CommonCodeController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}

	@Operation(summary = "Create a code group")
	@PostMapping("/groups")
	@ResponseStatus(HttpStatus.CREATED)
	public CodeGroupResponse createGroup(
			@RequestBody(required = false) CreateCodeGroupRequest request
	) {
		return commonCodeService.createGroup(request);
	}

	@Operation(summary = "Create a code detail")
	@PostMapping("/details")
	@ResponseStatus(HttpStatus.CREATED)
	public CodeDetailResponse createDetail(
			@RequestBody(required = false) CreateCodeDetailRequest request
	) {
		return commonCodeService.createDetail(request);
	}
}
