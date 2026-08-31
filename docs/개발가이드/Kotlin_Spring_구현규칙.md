# Kotlin/Spring 구현규칙

이 문서는 tumblingworks_Back-End에서 Kotlin으로 코드를 작성할 때 지키는 규칙이다. Java 코드에는 [Java_Kotlin_혼용_배치_규칙.md](Java_Kotlin_혼용_배치_규칙.md)에 따라 Java로 남겨둘지 Kotlin으로 옮길지부터 정하고, Kotlin으로 쓰기로 한 부분에 이 문서를 적용한다.

## 1. 기본 원칙

- 짧은 코드보다 명확한 null·예외·트랜잭션 계약을 우선한다.
- Java 쪽 기존 컨벤션(`common/exception`, `errorlog/*`의 스타일)과 충돌하지 않는 범위에서 Kotlin다운 표현을 쓴다.
- 요청 없이 persistence(JPA)나 동기 처리 모델을 바꾸지 않는다.
- 새 추상화(인터페이스, 공통 베이스 클래스)는 실제 다중 구현이나 교체 요구가 있을 때만 만든다.

## 2. 빌드 설정 — 확인된 것과 확인 필요

`build.gradle`에 적용된 플러그인:

```gradle
id 'org.jetbrains.kotlin.jvm' version '2.4.10'
id 'org.jetbrains.kotlin.plugin.spring' version '2.4.10'
```

- `kotlin.plugin.spring`이 있으므로 `@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration` 등이 붙은 Kotlin 클래스는 자동으로 `open` 처리된다. `@Transactional`, `@Async` 같은 프록시 기반 기능이 이 클래스들에서 정상 동작한다.
- **`kotlin-jpa` 플러그인은 없다.** 지금까지 Kotlin으로 작성된 `@Entity`가 없기 때문이다(`ErrorLog`는 Java). Kotlin으로 JPA `@Entity`를 작성하게 되면 이 시점에 `kotlin-jpa` 플러그인 추가 여부를 먼저 결정한다. 이 플러그인은 두 가지를 자동으로 해준다 — (1) `@Entity`/`@Embeddable`/`@MappedSuperclass`에 no-arg 생성자를 생성하고, (2) 해당 클래스를 `open`으로 만들어 Hibernate의 지연 로딩 프록시가 상속할 수 있게 한다. 플러그인 없이 Kotlin Entity를 만들면 이 두 가지(no-arg 생성자, `open` 클래스)를 모두 수동으로 처리해야 한다.
- `kotlin { jvmToolchain(26) }`으로 Java와 동일한 JDK 26을 사용한다.

## 3. 클래스와 값

### `val`과 `var`

- 재할당하지 않는 지역 변수·프로퍼티는 `val`로 선언한다.
- 상태 변경이 비즈니스 의미를 가질 때만 `var`를 쓴다.

### 최상위 함수 vs 클래스

현재 Kotlin 코드(`common/Utils.kt`)는 Spring Bean이 아니라 **순수 최상위 함수**로만 구성되어 있다.

```kotlin
@file:JvmName("Utils")

package com.tumblingworks.backend.common

fun isEmpty(value: String?): Boolean = value.isNullOrEmpty()
fun isBlank(value: String?): Boolean = value.isNullOrBlank()
```

- 상태가 없는 순수 유틸 함수는 이 패턴(`@file:JvmName`, top-level function)을 유지한다. `object Utils { ... }`로 감싸지 않는다 — Java 쪽에서 `Utils.isEmpty(...)`처럼 정적 메서드로 호출 가능해야 하기 때문이다(`@file:JvmName` 없이는 `UtilsKt.isEmpty(...)`가 되어 어색해진다).
- Spring이 관리해야 하는 로직(트랜잭션, DI 대상)은 최상위 함수가 아니라 `@Service`/`@Component` 클래스로 작성한다.

### `data class`

적합한 대상:

- Request/Response DTO
- 값 중심의 불변 전달 객체

주의 대상:

- JPA `@Entity` (지연 로딩 프록시, `equals`/`hashCode`가 관계를 건드릴 위험)
- 민감값이 `toString()`에 노출될 수 있는 객체

Java 쪽 DTO는 이미 `record`를 쓰고 있다(`ErrorResponse`, `ErrorLogCreateRequest`, `TestController`의 내부 record들). Kotlin에서 같은 역할의 DTO를 작성하면 `data class`가 Java `record`와 동등한 선택이다.

### 네이밍

- 클래스는 `PascalCase`, 함수·프로퍼티는 `camelCase`, 패키지는 소문자.
- 역할이 드러나는 `Controller`, `Service`, `Repository` 접미사를 Java와 동일하게 유지한다.

## 4. Nullability

- Java 코드와 인터페이스가 맞닿는 지점(예: `request.getAttribute(...)`처럼 Java 서블릿 API가 돌려주는 `Object`, `ErrorLogService`가 `RequestLifecycleInterceptor.REQUEST_ID_ATTRIBUTE` 키로 조회한 값)에서는 Java platform type을 무조건 non-null로 단정하지 않는다.
- `!!`는 null 불가능성이 외부 계약으로 증명될 때만 쓴다.
- `Utils.kt`의 `String?` 파라미터 스타일을 따른다 — nullable 입력을 받는 함수는 파라미터 타입에 `?`를 명시한다.

## 5. Spring Bean과 의존성 주입

- constructor injection을 기본으로 한다. Java 쪽 전 코드(`ErrorLogService`, `RequestLifecycleInterceptor`, `WebConfig` 등)가 모두 생성자 주입이므로 Kotlin도 동일하게 맞춘다.

```kotlin
@Service
class MemberService(
    private val memberRepository: MemberRepository,
)
```

- `lateinit`은 Spring field injection을 유지하기 위한 용도로 쓰지 않는다.

## 6. 트랜잭션

- 트랜잭션 경계는 [아키텍처_패턴_가이드.md](../아키텍처/아키텍처_패턴_가이드.md)에 따라 Service 계층에 둔다.
- 격리된 트랜잭션이 필요한 예외적 상황은 `ErrorLogService`의 `TransactionTemplate` + `PROPAGATION_REQUIRES_NEW` 패턴을 참고한다. 일반적인 경우는 `@Transactional`을 우선 검토하고, `TransactionTemplate`을 직접 쓰는 것은 메인 트랜잭션의 성공/실패와 무관하게 항상 커밋되어야 하는 경우(예: 에러 로깅)로 한정한다.

## 7. Java 상호운용

- Java에서 호출해야 하는 Kotlin 함수는 `@file:JvmName`으로 호출부 클래스명을 정리한다(`Utils.kt` 참고).
- static 유틸은 top-level function으로 작성하고, Java에서 `import static`으로 가져다 쓴다(`CommonTextService`의 `import static ...Utils.isEmpty`, `TestController`의 `import static ...Utils.isBlank` 참고).
- overload와 default argument가 Java 호출부에서 모호해지지 않는지 확인한다. 필요하면 `@JvmOverloads`를 사용한다.

## 8. 금지 목록

- 근거 없는 `!!`와 광범위한 `lateinit`
- Spring이 관리해야 할 로직을 상태 없는 최상위 함수로 만드는 것 (반대로 유틸 함수를 불필요하게 `@Component` 클래스로 감싸는 것도 금지)
- persistence 기술(JPA) 임의 교체
- 설계 논의 없는 계층·추상화 자동 추가

## 9. 관련 문서

- [아키텍처_패턴_가이드.md](../아키텍처/아키텍처_패턴_가이드.md)
- [Java_Kotlin_혼용_배치_규칙.md](Java_Kotlin_혼용_배치_규칙.md)
- [검증_테스트_기준.md](검증_테스트_기준.md)
