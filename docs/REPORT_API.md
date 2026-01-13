# Weekly Report API

본 문서는 주간 보고(Weekly Report) API 명세를 정리한다.

## 공통

- Base Path: `/api`
- Content-Type: `application/json`
- 응답 포맷: `ApiResponse<T>`
  - 성공: `{"success":true,"data":...,"errorCode":null,"message":null,"timestamp":"..."}`
  - 실패: `{"success":false,"data":null,"errorCode":"CODE","message":"...","timestamp":"..."}`

---

## 1) 전체 주간 보고 목록 조회 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/report`
- 설명: 접근 가능한 주간 보고 목록을 페이지 단위로 조회한다.

### Path Parameters

- `projectId`: Long

### Query Parameters

- `page` (optional) : 페이지 번호 (0부터 시작)
- `size` (optional) : 페이지 크기

### Response

`ApiResponse<Page<WeeklyReportSummaryResponse>>`

- `reportId`: Long
- `projectId`: Long
- `projectName`: String
- `reporterName`: String
- `weekStartDate`: `YYYY-MM-DD`
- `weekEndDate`: `YYYY-MM-DD`
- `weekLabel`: String (예: `2025년 1월 2주차`)
- `reportStatus`: `DRAFT | REVIEWED`
- `taskCompletionRate`: Double
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`

---

## 2) 전체 주간 보고 검색 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/report/search`
- 설명: 조건 기반으로 주간 보고 목록을 검색한다.

### Path Parameters

- `projectId`: Long

### Query Parameters

- `userId` (optional)
- `reportStatus` (optional) : `DRAFT | REVIEWED`
- `weekStartFrom` (optional) : `YYYY-MM-DD`
- `weekStartTo` (optional) : `YYYY-MM-DD`
- `keyword` (optional) : 프로젝트명 및 날짜 키워드
- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<WeeklyReportSummaryResponse>>`

---

## 3) 주간 보고 초안 생성 (Docs)

- Method: `POST`
- URL: `/api/projects/{projectId}/docs/report/create`
- 설명: 프로젝트/주차 기준 주간 보고 초안을 생성한다. (AI 생성은 스킵)

### Path Parameters

- `projectId`: Long

### Request Body

```
{
  "projectId": 77001,
  "weekStartDate": "2025-01-06",
  "weekEndDate": "2025-01-12"
}
```

### Response

`ApiResponse<WeeklyReportCreateResponse>`

- `reportId`: Long
- `projectId`: Long
- `projectName`: String
- `reporterName`: String
- `weekStartDate`: `YYYY-MM-DD`
- `weekEndDate`: `YYYY-MM-DD`
- `weekLabel`: String (예: `2025년 1월 2주차`)
- `reportStatus`: `DRAFT | REVIEWED`
- `taskCompletionRate`: Double
- `summaryText`: String | null
- `completedTasks`: List
- `incompleteTasks`: List
- `nextWeekTasks`: List

---

## 4) 주간 보고 상세 조회 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/report/{reportId}`
- 설명: 주간 보고 상세 정보를 조회한다.

### Path Variables

- `projectId`
- `reportId`

### Response

`ApiResponse<WeeklyReportDetailResponse>`

- `reportId`: Long
- `projectId`: Long
- `projectName`: String
- `reporterName`: String
- `weekStartDate`: `YYYY-MM-DD`
- `weekEndDate`: `YYYY-MM-DD`
- `weekLabel`: String (예: `2025년 1월 2주차`)
- `reportStatus`: `DRAFT | REVIEWED`
- `taskCompletionRate`: Double
- `summaryText`: String | null
- `changeOfPlan`: String | null
- `completedTasks`: List
  - `taskId`: Long
  - `taskName`: String
  - `assigneeName`: String
  - `taskCategory`: `DEVELOPMENT | TESTING | BUGFIXING | DISTRIBUTION`
  - `completionDate`: `YYYY-MM-DDTHH:mm:ss`
- `incompleteTasks`: List
  - `taskId`: Long
  - `taskName`: String
  - `assigneeName`: String
  - `taskCategory`: `DEVELOPMENT | TESTING | BUGFIXING | DISTRIBUTION`
  - `dueDate`: `YYYY-MM-DD`
  - `delayReason`: String | null
- `nextWeekTasks`: List
  - `taskId`: Long
  - `taskName`: String
  - `assigneeName`: String
  - `plannedStartDate`: `YYYY-MM-DD`
  - `plannedEndDate`: `YYYY-MM-DD`
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`

---

## 5) 주간 보고 수정/저장 (Docs)

- Method: `PATCH`
- URL: `/api/projects/{projectId}/docs/report/save`
- 설명: 주간 보고 초안을 수정하거나 상태를 변경한다.

### Path Parameters

- `projectId`: Long

### Request Body

```
{
  "reportId": 77001,
  "reportStatus": "REVIEWED",
  "changeOfPlan": "변경",
  "taskCompletionRate": 0.8,
  "completedTasks": [{"taskId": 77001}],
  "incompleteTasks": [{"taskId": 77002, "delayReason": "지연"}],
  "nextWeekTasks": [{"taskId": 77003, "plannedStartDate": "2025-01-13", "plannedEndDate": "2025-01-17"}]
}
```

### Response

`ApiResponse<WeeklyReportSaveResponse>`

- `reportId`: Long
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`

---

## 6) 주간 보고 삭제 (Docs)

- Method: `DELETE`
- URL: `/api/projects/{projectId}/docs/report/delete`
- 설명: 주간 보고를 소프트 삭제한다.

### Path Parameters

- `projectId`: Long

### Request Body

```
{
  "reportId": 77001
}
```

### Response

`ApiResponse<WeeklyReportDeleteResponse>`

- `reportId`: Long
- `isDeleted`: Boolean

---

## 7) 주간 보고 PDF 출력 (Docs)

- Method: `POST`
- URL: `/api/projects/{projectId}/docs/report/{reportId}/print`
- 설명: PDF 출력. (현재 미구현)

### Path Variables

- `projectId`
- `reportId`

### Response

- `501 Not Implemented`

---

## 8) 내 프로젝트 주간 보고 목록 조회 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/report`
- 설명: 로그인 사용자가 참여 중인 프로젝트의 주간 보고 목록만 조회한다.

### Query Parameters

- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<WeeklyReportSummaryResponse>>`

---

## 9) 내 프로젝트 주간 보고 검색 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/report/search`
- 설명: 내 프로젝트 범위 내에서 주간 보고 검색.

### Query Parameters

- `projectId` (optional)
- `reportStatus` (optional) : `DRAFT | REVIEWED`
- `weekStartFrom` (optional)
- `weekStartTo` (optional)
- `keyword` (optional)
- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<WeeklyReportSummaryResponse>>`

---

## 10) 내 프로젝트 주간 보고 상세 조회 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/report/{reportId}`
- 설명: 내 프로젝트에 속한 보고서 상세 조회.

### Response

`ApiResponse<WeeklyReportDetailResponse>`
