package com.tumblingworks.backend.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "cm_code_dtl")
public class CodeDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "code_id", nullable = false, updatable = false)
	private String codeId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private CodeGroup group;

	@Column(name = "v_dtl_code", nullable = false, length = 100)
	private String detailCode;

	@Column(name = "v_dtl_name", nullable = false, length = 200)
	private String detailCodeName;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "n_sort", nullable = false)
	private int sortOrder;

	@Column(name = "use_yn", nullable = false)
	private boolean useYn;

	@Column(name = "v_etc1", length = 200)
	private String extra1;

	@Column(name = "v_etc2", length = 200)
	private String extra2;

	@Column(name = "v_etc3", length = 200)
	private String extra3;

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

	protected CodeDetail() {
	}

	private CodeDetail(
			CodeGroup group,
			String detailCode,
			String detailCodeName,
			String description
	) {
		LocalDateTime now = LocalDateTime.now();
		this.group = group;
		this.detailCode = detailCode;
		this.detailCodeName = detailCodeName;
		this.description = description;
		this.sortOrder = 0;
		this.useYn = true;
		this.registeredAt = now;
		this.updatedAt = now;
		this.deletedFlag = "N";
	}

	public static CodeDetail create(
			CodeGroup group,
			String detailCode,
			String detailCodeName,
			String description
	) {
		return new CodeDetail(group, detailCode, detailCodeName, description);
	}

	public void update(
			String detailCodeName,
			String description,
			boolean useYn,
			int sortOrder,
			String extra1,
			String extra2,
			String extra3
	) {
		this.detailCodeName = detailCodeName;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
		this.extra1 = extra1;
		this.extra2 = extra2;
		this.extra3 = extra3;
	}

	public void markDeleted() {
		this.deletedFlag = DeletedFlag.DELETED;
	}

	@PreUpdate
	void updateTimestamp() {
		this.updatedAt = LocalDateTime.now();
	}

	public String getCodeId() {
		return codeId;
	}

	public String getGroupId() {
		return group.getGroupId();
	}

	public String getDetailCode() {
		return detailCode;
	}

	public String getDetailCodeName() {
		return detailCodeName;
	}

	public String getDescription() {
		return description;
	}

	public boolean isUseYn() {
		return useYn;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public String getExtra1() {
		return extra1;
	}

	public String getExtra2() {
		return extra2;
	}

	public String getExtra3() {
		return extra3;
	}

	public LocalDateTime getRegisteredAt() {
		return registeredAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getDeletedFlag() {
		return deletedFlag;
	}
}
