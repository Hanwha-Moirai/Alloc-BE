# JaCoCo 커버리지 가이드

이 문서는 `build.gradle` 기준으로 JaCoCo 커버리지 테스트가 무엇을 하는지, 리포트 생성/조회 방법, 그리고 결과 표 해석 방법을 정리합니다.

## 1) 이 프로젝트에서 JaCoCo가 하는 일

`build.gradle` 설정을 요약하면 다음과 같습니다.

- **JaCoCo 플러그인 활성화**: `jacoco` 플러그인을 통해 커버리지 측정/리포트 태스크를 사용합니다.
- **버전 고정**: `toolVersion = "0.8.10"`으로 리포트 일관성을 유지합니다.
- **측정 대상 제한**:
  - 리포트 대상 클래스는 `**/*Controller*`, `**/*Service*`로 제한됩니다.
  - `config`, `dto`, `entity`, `exception`, `model`, `util`, `common` 등은 제외됩니다.
- **리포트 형식**:
  - XML/HTML 리포트가 생성됩니다.
  - CSV는 생성하지 않습니다.
- **테스트 후 리포트 자동 생성**:
  - `test` 태스크가 끝나면 `jacocoTestReport`가 항상 실행됩니다.
  - 테스트 실패 시에도 리포트 생성은 시도됩니다.

즉, 이 프로젝트에서는 **Controller/Service 중심의 커버리지 리포트**를 자동으로 생성하도록 구성되어 있습니다.

## 2) JaCoCo 리포트 생성 방법

다음 중 하나를 실행하면 됩니다.

```bash
./gradlew test
```

또는 리포트만 직접 실행하려면:

```bash
./gradlew jacocoTestReport
```

`test` 태스크는 끝나면 자동으로 `jacocoTestReport`가 실행되므로 보통은 `./gradlew test`만 실행하면 됩니다.

## 3) JaCoCo 리포트 보는 방법

HTML 리포트는 다음 위치에 생성됩니다.

```
build/reports/jacoco/test/html/index.html
```

브라우저에서 해당 파일을 열면 리포트를 확인할 수 있습니다.
혹은 http://localhost:63342/Alloc/build/reports/jacoco/test/html/index.html 로 들어가셔서 볼 수 있습니다.

## 4) 리포트 표(Results) 해석 방법

JaCoCo HTML 리포트의 주요 컬럼 의미는 다음과 같습니다.

- **Missed Instructions / Cov.**: 바이트코드 명령어 기준으로 실행되지 않은 수와 커버리지 비율
- **Missed Branches / Cov.**: 분기(if/else 등) 중 실행되지 않은 수와 커버리지 비율
- **Missed Lines / Cov.**: 실행되지 않은 소스 코드 라인 수와 라인 커버리지 비율
- **Missed Methods / Cov.**: 실행되지 않은 메서드 수와 메서드 커버리지 비율
- **Missed Classes / Cov.**: 실행되지 않은 클래스 수와 클래스 커버리지 비율

일반적으로는 **Line 커버리지**와 **Branch 커버리지**를 많이 확인합니다.

- Line 커버리지가 낮으면: 테스트가 실제 코드 실행을 충분히 커버하지 못하고 있다는 뜻입니다.
- Branch 커버리지가 낮으면: 조건 분기(참/거짓) 중 한쪽만 실행된 경우가 많다는 뜻입니다.

리포트의 패키지/클래스/메서드 단위로 클릭해 들어가면
**어떤 라인이 미실행(붉은색), 부분 실행(노란색), 완전 실행(초록색)**인지 확인할 수 있습니다.

## 5) 참고: 커버리지 대상/제외 기준

현재 설정 기준으로 리포트 대상은 다음과 같습니다.

- 포함: `**/*Controller*`, `**/*Service*`
- 제외: `config`, `dto`, `entity`, `exception`, `model`, `util`, `common`, 그리고 QueryDSL `Q` 도메인 클래스

커버리지 범위를 늘리거나 줄이고 싶다면 `build.gradle`의 `classDirectories`와 `jacocoExcludes` 설정을 조정하면 됩니다.
