tumblingworks_Back-End 현재 스펙

[Swagger 경로]
 : /swagger-ui/index.html

[기본 정보]
 : 프로젝트명	tumblingworks_Back-End
 : 빌드 도구	Gradle Wrapper
 : 기본 브랜치	main

[기술 스택]
: Language   : Java 26, Kotlin 2.4.10
: Framework  : Spring Boot 4.1.0

[설정]
File → Project Structure → Project
 : JDK 26

Settings → Build, Execution, Deployment → Build Tools → Gradle
 : Gradle JVM: Project SDK (Java 26)

Run → Edit Configurations → TumblingworksBackEndApplication
 : jdk-26.0.2

[인터셉터 경로 설정]
addInterceptors
 : /api/**

[CORS 설정]
 : /api/**