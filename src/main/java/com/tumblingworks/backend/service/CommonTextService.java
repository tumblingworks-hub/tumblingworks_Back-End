package com.tumblingworks.backend.service;

import org.springframework.stereotype.Service;

import static com.tumblingworks.backend.common.Utils.isEmpty;

@Service
public class CommonTextService {

	public boolean isEmptyText(String value) {
		return isEmpty(value);
	}
}
