package com.tumblingworks.backend.code.repository;

import com.tumblingworks.backend.code.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, String> {

	boolean existsByGroupCode(String groupCode);

	List<CodeGroup> findAllByDeletedFlagOrderBySortOrderAscGroupCodeAsc(
			String deletedFlag
	);

	Optional<CodeGroup> findByGroupIdAndDeletedFlag(
			String groupId,
			String deletedFlag
	);
}
