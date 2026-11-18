tumblingworks_Back-End 현재 스펙

[Swagger 경로]
 : /swagger-ui/index.html

[기본 정보]
 : 프로젝트명	tumblingworks_Back-End
 : 빌드 도구	Gradle Wrapper
 : 기본 브랜치	main

[기술 스택]
: Language   : Java 25 LTS, Kotlin 2.4.10
: Framework  : Spring Boot 4.1.0

[설정]
File → Project Structure → Project
 : Oracle JDK 25.0.5.1 이상 (Java 25 LTS)

Settings → Build, Execution, Deployment → Build Tools → Gradle
 : Gradle JVM: Project SDK (Java 25)

Run → Edit Configurations → TumblingworksBackEndApplication
 : Project SDK (Java 25)

Docker 빌드와 실행에는 동일한 Oracle JDK 25를 사용합니다. 이미지를 빌드할 때
다운로드 체크섬을 검증하고, CVE-2026-114938이 수정된 25.0.5.1 이상인지 확인합니다.

[인터셉터 경로 설정]
addInterceptors
 : /api/**

[CORS 설정]
 : /api/**
