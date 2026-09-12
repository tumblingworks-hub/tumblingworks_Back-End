package com.tumblingworks.backend.code.repository;

import com.tumblingworks.backend.code.entity.CodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeDetailRepository extends JpaRepository<CodeDetail, String> {

	boolean existsByGroup_GroupIdAndDetailCode(
			String groupId,
			String detailCode
	);
}
