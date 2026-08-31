# CLAUDE.md — tumblingworks_Back-End

이 파일은 얇은 진입점이다. 상세 규칙은 아래 문서가 소유하며, 여기서는 요약과 링크만 둔다.

## 프로젝트 개요

- Tumblingworks Back-End REST API. Java와 Kotlin을 한 모듈에서 함께 쓰는 그린필드 프로젝트(마이그레이션 대상 원본 없음).
- 스택: Java 26 (JDK toolchain), Kotlin 2.4.10, Spring Boot 4.1.0, Spring Data JPA, PostgreSQL, springdoc-openapi.
- 상세 스펙·Swagger 경로·IDE 설정은 [README.md](README.md) 참고.

## 개발 가이드 문서

새 기능을 추가하거나 기존 코드를 수정하기 전에 관련 문서를 확인한다.

- [docs/아키텍처/아키텍처_패턴_가이드.md](docs/아키텍처/아키텍처_패턴_가이드.md) — 계층 구조(MVC 고정), 패키지 구조(계층형), 계층별 책임, 검증 규칙
- [docs/개발가이드/Kotlin_Spring_구현규칙.md](docs/개발가이드/Kotlin_Spring_구현규칙.md) — Kotlin 코드 작성 규칙 (val/var, data class, null 처리, DI, 트랜잭션, Java 상호운용)
- [docs/개발가이드/Java_Kotlin_혼용_배치_규칙.md](docs/개발가이드/Java_Kotlin_혼용_배치_규칙.md) — 새 코드를 Java로 쓸지 Kotlin으로 쓸지 정하는 기준
- [docs/개발가이드/검증_테스트_기준.md](docs/개발가이드/검증_테스트_기준.md) — 테스트 스택, 계층별 테스트 방식, 빌드/검증 명령

## 최소 안전수칙

- DB 스키마 관리 방식은 [아키텍처_패턴_가이드.md](docs/아키텍처/아키텍처_패턴_가이드.md) §6 참고 — 새 Entity/컬럼 작업 전에 반드시 확인한다.
- `/config/application-secrets.properties`(로컬 비밀값)는 `.gitignore`에 있다. 내용을 커밋하거나 문서·로그에 옮겨 적지 않는다.
- 빌드/테스트 명령은 [검증_테스트_기준.md](docs/개발가이드/검증_테스트_기준.md) 참고.
- 요청 없이 `git add`/`commit`/`push`를 실행하지 않는다.
