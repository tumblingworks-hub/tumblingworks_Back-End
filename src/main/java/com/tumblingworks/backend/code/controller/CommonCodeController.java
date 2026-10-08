package com.tumblingworks.backend.code.controller;

import com.tumblingworks.backend.code.dto.CodeDetailResponse;
import com.tumblingworks.backend.code.dto.CodeGroupResponse;
import com.tumblingworks.backend.code.dto.CreateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.CreateCodeGroupRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeDetailRequest;
import com.tumblingworks.backend.code.dto.UpdateCodeGroupRequest;
import com.tumblingworks.backend.code.service.CommonCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

	@Operation(summary = "List code groups")
	@GetMapping("/groups")
	public List<CodeGroupResponse> getGroups() {
		return commonCodeService.getGroups();
	}

	@Operation(summary = "Get a code group")
	@GetMapping("/groups/{groupId}")
	public CodeGroupResponse getGroup(@PathVariable String groupId) {
		return commonCodeService.getGroup(groupId);
	}

	@Operation(summary = "Update a code group")
	@PutMapping("/groups/{groupId}")
	public CodeGroupResponse updateGroup(
			@PathVariable String groupId,
			@RequestBody(required = false) UpdateCodeGroupRequest request
	) {
		return commonCodeService.updateGroup(groupId, request);
	}

	@Operation(summary = "Delete a code group")
	@DeleteMapping("/groups/{groupId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteGroup(@PathVariable String groupId) {
		commonCodeService.deleteGroup(groupId);
	}

	@Operation(summary = "List code details of a group")
	@GetMapping("/groups/{groupId}/details")
	public List<CodeDetailResponse> getDetails(@PathVariable String groupId) {
		return commonCodeService.getDetails(groupId);
	}

	@Operation(summary = "Create a code detail")
	@PostMapping("/details")
	@ResponseStatus(HttpStatus.CREATED)
	public CodeDetailResponse createDetail(
			@RequestBody(required = false) CreateCodeDetailRequest request
	) {
		return commonCodeService.createDetail(request);
	}

	@Operation(summary = "Get a code detail")
	@GetMapping("/details/{codeId}")
	public CodeDetailResponse getDetail(@PathVariable String codeId) {
		return commonCodeService.getDetail(codeId);
	}

	@Operation(summary = "Update a code detail")
	@PutMapping("/details/{codeId}")
	public CodeDetailResponse updateDetail(
			@PathVariable String codeId,
			@RequestBody(required = false) UpdateCodeDetailRequest request
	) {
		return commonCodeService.updateDetail(codeId, request);
	}

	@Operation(summary = "Delete a code detail")
	@DeleteMapping("/details/{codeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDetail(@PathVariable String codeId) {
		commonCodeService.deleteDetail(codeId);
	}
}
