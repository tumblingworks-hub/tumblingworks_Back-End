---
description: 컨트롤러 메서드의 @Operation(springdoc) description(요청/응답 필드표·중첩 하위표·에러 응답표)과 @Schema example 값을 코드에서 자동 생성/갱신.
argument-hint: <컨트롤러 파일 경로> [메서드명]
---

컨트롤러 파일을 분석해 `@Operation`의 `description`을 생성하고 파일에 직접 삽입/갱신한다. springdoc-openapi(OpenAPI v3) 기준이며, 이 프로젝트에 실제로 존재하는 코드(`GlobalExceptionHandler`, Bean Validation, Java record DTO)만 근거로 삼는다.

## 인자

- `$1` : 컨트롤러 파일 경로 (필수)
- `$2` : 메서드명 (선택) — 없으면 파일 내 모든 `@GetMapping`·`@PostMapping`·`@PutMapping`·`@PatchMapping`·`@DeleteMapping` 메서드를 대상으로 한다

## 실행 절차

### 1단계 — 컨트롤러 파일 읽기

`$1` 파일을 읽는다. `$2`가 있으면 해당 메서드만, 없으면 모든 매핑 메서드를 대상으로 한다.

### 2단계 — 에러 응답 수집 (이 프로젝트 규모에 맞춘 단순 버전)

이 프로젝트의 에러 응답은 `GlobalExceptionHandler`(`@RestControllerAdvice`) 하나가 전담하며, 등록된 핸들러는 현재 둘뿐이다: `MethodArgumentNotValidException` → 400 `VALIDATION_ERROR`, 그 외 모든 `Exception` → 500 `INTERNAL_SERVER_ERROR`. 커스텀 예외 클래스·계층별 에러 코드(processCode/processErrorLayer 같은)는 존재하지 않는다. 따라서:

**2-1. 검증 오류 행 여부 판단**
- 메서드의 요청 파라미터(`@RequestBody` DTO에 `@Valid`, 또는 `@RequestParam`/`@PathVariable`에 Bean Validation 제약)에 `jakarta.validation.constraints.*` 애노테이션이 하나라도 있으면 400 `VALIDATION_ERROR` 행을 추가한다.
- 없으면 이 행을 만들지 않는다(추측으로 만들지 않는다).

**2-2. 항상 추가**
- 500 `INTERNAL_SERVER_ERROR` — "예기치 않은 서버 오류" 행은 모든 메서드에 추가한다. `GlobalExceptionHandler`의 catch-all이 항상 이 응답을 만들기 때문이다.

**2-3. 미처리 예외 발견 시 — 표에 넣지 말고 경고**
- 메서드 바디 또는 1홉 호출(Service 메서드 1개)에서 명시적으로 `throw`하는 커스텀 예외 타입을 발견했는데 `GlobalExceptionHandler`에 그 타입 전용 핸들러가 없으면, **새 에러 코드를 지어내지 말고** 사용자에게 보고한다: "`XyzException`이 던져지지만 전용 핸들러가 없어 500(`INTERNAL_SERVER_ERROR`)으로 처리됩니다 — 의도한 동작인가요?" 이 예외는 그래도 §2-2의 500 행으로 커버되므로 문서 자체는 완성된다.
- **Spring이 직접 던지는 바인딩 예외도 같은 방식으로 확인한다.** `@RequestParam`이 `required=true`(기본값)인데 값이 없으면 `MissingServletRequestParameterException`, `@RequestParam`/`@PathVariable`의 타입 변환이 실패하면 `MethodArgumentTypeMismatchException`을 Spring이 던진다 — 코드가 명시적으로 `throw`하지 않아도 발생한다. (`@PathVariable` 값 자체가 없는 경우는 라우팅이 매칭되지 않아 보통 404가 되므로 이 두 예외와는 다른 경로다.) `GlobalExceptionHandler`에 이 둘의 전용 핸들러가 없으므로 현재는 500으로 처리된다(보통 400이 기대되는 상황이라 갭일 가능성이 높다). 필수 원시 파라미터나 타입 변환이 있는 메서드에서는 이것도 §2-1과 별개로 확인하고 같은 형식으로 보고한다.
- 재귀는 Controller → 호출된 Service 메서드 딱 1홉까지만. 그 이상 따라가지 않는다(이 프로젝트는 Controller→Service→Repository 외에 별도 facade/app/infra 다단 계층을 두지 않음).

**표 형식 참고** — 모든 응답이 공유하는 `ErrorResponse(code, message, requestId, path, timestamp)` 계약은 메서드마다 반복 설명하지 않고 한 번만 언급한다(예: description 하단에 "에러 응답 형식은 `ErrorResponse` 공통" 한 줄).

### 3단계 — 요청/응답 필드 표 수집

**3-1. 요청 필드** (컬럼: `필드 | 타입 | 필수 | 설명`)
- `@RequestBody` DTO(보통 Java `record`)면 각 record 컴포넌트를 필드로 수집한다.
- 필수 여부:
  - 원시 타입(`int`, `boolean`, `long`, `double` 등)은 Java 언어 자체가 null을 허용하지 않으므로 항상 `Y`.
  - 참조 타입에 `@NotNull`/`@NotBlank`/`@NotEmpty`가 있으면 `Y`, 없으면 `N`(nullable로 간주).
- 원시 파라미터(`@RequestParam`/`@PathVariable`)는 **Spring 어노테이션 자체의 `required` 속성을 그대로 쓴다** — `@RequestParam(required = false)`면 `N`, 기본값(`true`)이거나 `@PathVariable`이면 `Y`. 메서드 바디의 가드 조건문을 스캔할 필요 없음 — Spring은 이 정보를 어노테이션에 이미 선언한다.

**3-2. 응답 필드** (컬럼: `필드 | 타입 | 설명`)
- 리턴 타입 record의 각 컴포넌트를 수집한다.

**3-3. 중첩 DTO 재귀 — 하위표**
- 필드 타입이 이름 있는 DTO record/클래스이거나 그 배열/`List<T>`면 하위표(`필드 | 타입 | 설명`)를 별도로 만든다. 하위표 제목은 배열/`List`면 `**{필드명}[] 항목**`, 단건이면 `**{필드명} 항목**`.
- 재귀하지 않는 타입: `Map<K,V>`, 원시타입, `String`/`Instant`/`OffsetDateTime` 같은 표준 라이브러리 타입, enum(값 목록만 설명 칸에 나열).
- 지금까지 이 프로젝트의 어떤 DTO도 DTO 필드를 갖지 않는다(전부 평평한 record) — 이 규칙은 첫 중첩 사례가 생길 때 실제로 쓰인다.

**3-4. 설명(마지막 칸) 소스 체인** — 처음 값이 있는 것을 쓴다.
1. 필드에 이미 있는 `@Schema(description = ...)` 값 (6단계에서 넣은 것이 있으면 그대로 재사용 — 매 실행 문구가 흔들리지 않게)
2. Bean Validation 제약 → 텍스트 변환: `@Size(max=20)` → "최대 20자", `@Size(min=2,max=20)` → "2~20자", `@Min`/`@Max` → 범위, `@Email` → "이메일 형식", `@Pattern` → "형식 제약 있음(정규식 확인)"
3. 필드명 기반 추론 (`memberId` → "회원 ID"). 이때 메서드의 기존 `summary`/`description`도 문맥으로 함께 본다 — 예를 들어 "Check whether a value is blank" 옆의 원시 파라미터 `value`는 필드명만으론 모호하지만 summary와 합치면 "검사할 값"으로 추론할 수 있다.
4. 제약 텍스트(2)와 이름추론(3)을 합쳐 한 칸에 적는다 — 예: `@Size(max=20)` + `memberId` → "회원 ID (최대 20자)"

**3-5. 예제 값 생성 — `@Schema(example = ...)`을 record 컴포넌트에 직접 붙인다**

이 방식은 이 프로젝트가 실제로 쓰는 버전(`springdoc-openapi-starter-webmvc-ui:3.0.3` → `swagger-core-jakarta:2.2.47`)에서 동작 확인됨: record 컴포넌트의 `@Schema(description, example)`이 `/v3/api-docs` 스키마에 그대로 반영된다. 컨트롤러 데코레이터에 별도 예제 블록을 만들지 않는다 — 이 프로젝트의 DTO는 (지금까지) 오퍼레이션마다 로컬 record라서 필드 옆에 다는 쪽이 한 곳에서 관리된다.

- 예제 값 우선순위: ① 필드에 이미 있는 `@Schema(example=...)` → 그대로 유지 ② 제약에 맞는 값(`@Size(max=20)` → 20자 이내 문자열, `@Min`/`@Max` → 경계값) ③ 필드명/타입 기반 합성
- 원시 `@RequestParam`/`@PathVariable`은 record가 아니므로 예제를 붙일 필드가 없다 — `@Parameter(example = "...")`(springdoc 어노테이션)를 해당 파라미터에 직접 붙인다.
- 예제 값은 **합성/저자재량** 영역이다(3-4의 산문과 동일 성격) — 8단계 결정성 보장 대상 아님. 최초 생성 후 6단계 보존 규칙으로 고정된다.

### 4단계 — description 생성

```
{기존 summary 첫 줄 또는 메서드명 기반 요약}.

**요청 필드**

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| value | string | N | 검사할 값 |

**응답 필드**

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| value | string | 입력값 그대로 반환 |
| blank | boolean | 공백 여부 |

**{중첩필드}[] 항목**          ← 3-3에서 수집한 중첩 DTO가 있을 때만

**에러 응답**                  ← 2단계에서 만든 행이 있을 때만. 형식은 공통 `ErrorResponse` 참고 한 줄 + 아래 표

| HTTP 상태 | code | 설명 |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | 요청값 검증 실패 |
| 500 | `INTERNAL_SERVER_ERROR` | 예기치 않은 서버 오류 |
```

- 요청/응답 필드표는 요청 파라미터/응답 타입이 있는 한 항상 생성한다. 에러 응답표는 500 행이 항상 최소 1개 존재하므로 사실상 매번 생성된다.
- 마크다운 셀 안의 `|`(유니온·제네릭 등)는 `\|`로 이스케이프하거나 `string / null`처럼 슬래시로 표기한다.

### 5단계 — 파일 삽입/갱신

**5-0. ★ 기존 내용이 있으면 먼저 사용자에게 물어본다 (AskUserQuestion)**

대상 메서드에 이미 `@Operation` description(표·본문) 또는 필드에 `@Schema`가 존재하면, 고치기 전에 사용자에게 묻는다.
- **보강(preserve)**: 기존 내용 최대 보존 — 에러 응답표만 갱신(핸들러 변경 반영)하고, 필드표 설명·example 값은 유지.
- **재생성(regenerate)**: 기존 description·필드표·example을 밀고 스킬 출력으로 새로 작성.
- 예외: `@Operation` 자체가 없거나 description이 비어 있으면(=신규) 묻지 않고 바로 생성한다. 메서드가 여럿이면 "기존 있는 메서드"에 한해 한 번만 묻고 파일 전체에 같은 방침을 적용한다.

- `@Operation`이 **없으면**: 매핑 애노테이션 바로 위에 `@Operation(summary = "...", description = "...")` 추가 (신규 → 질문 생략)
- **보강** 선택 시: 에러 응답표는 갱신, 필드표의 수기 보강 설명·`@Schema(example=...)` 값은 보존하고 필드 추가/삭제만 반영. `summary`는 건드리지 않는다.
- **재생성** 선택 시: description·필드표·example 모두 재도출값으로 덮어쓴다.

### 6단계 — 빌드 확인

`./gradlew build`를 실행해 컴파일 오류가 없는지 확인한다. (주의: 이 커맨드를 만든 세션은 로컬 JDK가 26이 아니어서 이 단계를 실제로 실행·검증하지 못했다 — 처음 실행할 때 이 단계가 실제로 통과하는지 직접 확인할 것.)

### 7단계 — 멱등성 자가검증

같은 파일에 스킬을 한 번 더 돌려서 diff가 없는지 확인한다.

**결정적(재현) 보장 범위**: 필드 구조·타입·필수 여부, Bean Validation에서 나온 제약 텍스트, 에러 응답표.
**합성(author-tunable) 영역**: 이름 추론에서 나온 설명 산문, example 값 — 최초 생성 후 5단계 보존 규칙으로 고정되어 재실행 시 diff 없어야 한다.

diff가 생기면: 구조/제약/에러표가 흔들렸으면 버그, 합성 산문만 흔들렸으면 5단계 보존 규칙 누락이다.
