package com.tumblingworks.backend.code.repository;

import com.tumblingworks.backend.code.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, String> {

	boolean existsByGroupCode(String groupCode);
}
