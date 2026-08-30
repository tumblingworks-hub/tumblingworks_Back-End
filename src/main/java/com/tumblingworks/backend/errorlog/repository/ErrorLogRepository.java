package com.tumblingworks.backend.errorlog.repository;

import com.tumblingworks.backend.errorlog.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, String> {

	Optional<ErrorLog> findByRequestId(String requestId);
}
