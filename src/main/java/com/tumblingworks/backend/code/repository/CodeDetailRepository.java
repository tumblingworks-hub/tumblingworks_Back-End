package com.tumblingworks.backend.code.repository;

import com.tumblingworks.backend.code.entity.CodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeDetailRepository extends JpaRepository<CodeDetail, String> {

	boolean existsByGroup_GroupIdAndDetailCode(
			String groupId,
			String detailCode
	);

	boolean existsByGroup_GroupIdAndDeletedFlag(
			String groupId,
			String deletedFlag
	);

	List<CodeDetail> findAllByGroup_GroupIdAndDeletedFlagOrderBySortOrderAscDetailCodeAsc(
			String groupId,
			String deletedFlag
	);

	Optional<CodeDetail> findByCodeIdAndDeletedFlag(
			String codeId,
			String deletedFlag
	);
}
