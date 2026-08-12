package com.tumblingworks.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonTextServiceTests {

	private final CommonTextService commonTextService = new CommonTextService();

	@Test
	void javaServiceCallsKotlinUtilityForNullAndEmptyValues() {
		assertThat(commonTextService.isEmptyText(null)).isTrue();
		assertThat(commonTextService.isEmptyText("")).isTrue();
	}

	@Test
	void javaServiceCallsKotlinUtilityForNonEmptyValue() {
		assertThat(commonTextService.isEmptyText("tumblingworks")).isFalse();
	}
}
