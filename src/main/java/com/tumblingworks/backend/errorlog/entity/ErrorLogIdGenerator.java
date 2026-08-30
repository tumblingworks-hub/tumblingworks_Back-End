package com.tumblingworks.backend.errorlog.entity;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class ErrorLogIdGenerator implements IdentifierGenerator {

	@Override
	public Object generate(
			SharedSessionContractImplementor session,
			Object object
	) {
		return session.createNativeQuery(
				"select 'ERR' || generate_error_log_id()",
				String.class
		).getSingleResult();
	}
}
