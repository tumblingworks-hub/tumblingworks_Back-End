# Java/Kotlin 혼용 배치 규칙

tumblingworks_Back-End는 Java와 Kotlin을 한 모듈(`src/main/java`, `src/main/kotlin`)에서 함께 쓴다. 이 문서는 "기존 Java를 Kotlin으로 전환"하는 정책이 아니라, **새 코드를 작성할 때 어느 언어로 쓸지 정하는 기준**이다.

## 1. 현재 상태

- `src/main/java`: Spring이 관리하는 컴포넌트(Controller, Service, Repository, `@Entity`, `@Configuration`, `@RestControllerAdvice`)와, Spring과 무관한 지원 코드(`errorlog`의 DTO record, 커스텀 ID 생성 애노테이션·`IdentifierGenerator`)가 모두 Java로 되어 있다.
- `src/main/kotlin`: `common/Utils.kt` — Spring Bean이 아닌 순수 함수 하나뿐.

즉 지금까지의 실제 선택은 "Spring Bean이든 아니든 Java가 기본, 상태 없는 순수 유틸 함수만 Kotlin"이다. 이 문서는 그 경계를 규칙으로 명시한다.

## 2. 새 코드를 어디에 둘지 결정하는 기준

| 코드 성격 | 언어 | 이유 |
|---|---|---|
| `@Controller`, `@Service`, `@Repository`, `@Configuration`, `@Entity` 등 Spring이 관리하는 컴포넌트 | **Java** | 지금까지 전 컴포넌트가 Java. `kotlin-jpa` 플러그인 없이 Kotlin Entity를 만들면 no-arg 생성자를 수동 처리해야 함(§4 참고) |
| 상태 없는 순수 함수(문자열/컬렉션 검사, 포맷 변환 등) | **Kotlin** | `Utils.kt` 선례. `@file:JvmName`으로 Java에서 자연스럽게 호출 가능 |
| DTO(record/data class) | 확정 안 됨 | 현재는 전부 Java `record`. Kotlin `data class`로 새로 만들 수도 있으나, 한 기능 안에서 요청 DTO는 Java record, 응답 DTO는 Kotlin data class처럼 무작정 섞지 않는다 — 같은 기능 내 DTO는 한 언어로 통일한다 |

이 표에 없는 새로운 성격의 코드(예: 배치, 이벤트 리스너, 외부 API 클라이언트)가 생기면 이 표에 추가하고 판단 근거를 남긴다. 표에 없다고 임의로 결정하지 않는다.

## 3. Kotlin으로 새 Spring 컴포넌트를 쓰기로 할 때

위 표는 현재 기본값이며, 사용자가 특정 기능을 Kotlin Service/Controller로 작성하기로 명시적으로 정하면 그 결정을 따른다. 이때 다음을 먼저 확인한다.

- `kotlin.plugin.spring`이 이미 적용되어 있으므로 `open` 처리는 자동이다(추가 설정 불필요).
- `@Entity`를 Kotlin으로 작성하려면 `kotlin-jpa` 플러그인 추가 여부를 먼저 결정한다(§4).
- 생성자 주입 스타일, 네이밍, nullability 등은 [Kotlin_Spring_구현규칙.md](Kotlin_Spring_구현규칙.md)를 따른다.

## 4. Kotlin Entity를 도입하기로 할 경우 — 확인 필요

아직 발생하지 않은 상황이지만, 미리 남겨둔다.

- `build.gradle`에 `kotlin-jpa` 플러그인이 없다. 추가하지 않으면 Kotlin `@Entity` 클래스에 JPA가 요구하는 no-arg 생성자를 직접 열어줘야 한다(`ErrorLog`처럼 protected 기본 생성자를 Kotlin 문법으로 재현).
- `ErrorLog`가 쓰는 커스텀 ID 생성 방식(`@ErrorLogGeneratedId` + `IdentifierGenerator` + DB 함수 `generate_error_log_id()`)은 Java 애노테이션 기반이다. Kotlin Entity에서 같은 패턴을 쓰려면 애노테이션 타겟(`@Target(FIELD)`)이 Kotlin 프로퍼티에 그대로 적용되는지 먼저 확인한다.

## 5. 금지 목록

- 같은 클래스나 같은 기능의 관련 클래스들을 이유 없이 Java/Kotlin으로 나누어 쓰지 않는다(예: 같은 기능의 Controller는 Java, Service는 Kotlin으로 무작정 나누기).
- "Kotlin이 최신이니까"라는 이유만으로 기존 Java 컴포넌트를 재작성하지 않는다. 언어 전환 자체가 목적인 작업은 이 프로젝트 범위에 없다(그린필드 개발이며 마이그레이션 대상 원본이 없음).
- Kotlin 최상위 함수에 Spring 컨텍스트가 필요한 로직(트랜잭션, DB 접근)을 넣지 않는다.

## 6. 관련 문서

- [아키텍처_패턴_가이드.md](../아키텍처/아키텍처_패턴_가이드.md)
- [Kotlin_Spring_구현규칙.md](Kotlin_Spring_구현규칙.md)
- [검증_테스트_기준.md](검증_테스트_기준.md)
