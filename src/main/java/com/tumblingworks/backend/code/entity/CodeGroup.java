package com.tumblingworks.backend.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "cm_code_group")
public class CodeGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_id", nullable = false, updatable = false)
	private String groupId;

	@Column(name = "group_code", nullable = false, length = 100)
	private String groupCode;

	@Column(name = "group_name", nullable = false, length = 200)
	private String groupName;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "use_yn", nullable = false)
	private boolean useYn;

	@Column(name = "n_sort", nullable = false)
	private int sortOrder;

	@Column(name = "regist_dtm", nullable = false, updatable = false)
	private LocalDateTime registeredAt;

	@Column(name = "regist_user_id", length = 100, updatable = false)
	private String registeredUserId;

	@Column(name = "update_dtm", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "update_user_id", length = 100)
	private String updatedUserId;

	@Column(name = "v_flag_del", nullable = false, length = 10)
	private String deletedFlag;

	protected CodeGroup() {
	}

	private CodeGroup(String groupCode, String groupName, String description) {
		LocalDateTime now = LocalDateTime.now();
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.description = description;
		this.useYn = true;
		this.sortOrder = 0;
		this.registeredAt = now;
		this.updatedAt = now;
		this.deletedFlag = "N";
	}

	public static CodeGroup create(
			String groupCode,
			String groupName,
			String description
	) {
		return new CodeGroup(groupCode, groupName, description);
	}

	@PreUpdate
	void updateTimestamp() {
		this.updatedAt = LocalDateTime.now();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public String getGroupName() {
		return groupName;
	}
}
