package com.tumblingworks.backend.code.entity;

/**
 * cm_code_group / cm_code_dtl 의 v_flag_del 값.
 * 등록 코드가 "N"을 쓰는 것은 확인했지만, 삭제 값 "Y"는 DDL이 저장소에 없어 확인하지 못한 가정이다.
 */
public final class DeletedFlag {

	public static final String ACTIVE = "N";
	public static final String DELETED = "Y";

	private DeletedFlag() {
	}
}
