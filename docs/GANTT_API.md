# Gantt API

본 문서는 간트(Task/Milestone) API 명세를 정리한다.

## 공통

- Base Path: `/api`
- Content-Type: `application/json`
- 응답 포맷: `ApiResponse<T>`
  - 성공: `{"success":true,"data":...,"errorCode":null,"message":null,"timestamp":"..."}`
  - 실패: `{"success":false,"data":null,"errorCode":"CODE","message":"...","timestamp":"..."}`

---

## 1) 태스크 조회

- Method: `GET`
- URL: `/api/projects/{projectId}/tasks`
- 설명: 태스크 목록을 조회한다. (임박/필터 통합)

### Query Parameters

- `assigneeId` (optional)
- `status` (optional) : `TODO | INPROGRESS | DONE`
- `startDate` (optional) : `YYYY-MM-DD`
- `endDate` (optional) : `YYYY-MM-DD`

### Response

`ApiResponse<List<TaskResponse>>`

- `taskId`: Long
- `milestoneId`: Long
- `userId`: Long
- `taskCategory`: `DEVELOPMENT | TESTING | BUGFIXING | DISTRIBUTION`
- `taskName`: String
- `taskDescription`: String
- `taskStatus`: `TODO | INPROGRESS | DONE`
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`
- `startDate`: `YYYY-MM-DD`
- `endDate`: `YYYY-MM-DD`
- `isCompleted`: Boolean
- `isDeleted`: Boolean

---

## 2) 태스크 생성

- Method: `POST`
- URL: `/api/projects/{projectId}/tasks`

### Request Body

```
{
  "milestoneId": 1001,
  "assigneeId": 2001,
  "taskCategory": "DEVELOPMENT",
  "taskName": "API 구현",
  "taskDescription": "desc",
  "startDate": "2025-01-03",
  "endDate": "2025-01-05"
}
```

### Response

`ApiResponse<CreatedIdResponse>`

- `id`: Long

---

## 3) 태스크 수정

- Method: `PATCH`
- URL: `/api/projects/{projectId}/tasks/{taskId}`

### Request Body

```
{
  "milestoneId": 1001,
  "assigneeId": 2001,
  "taskCategory": "DEVELOPMENT",
  "taskName": "수정된 제목",
  "taskDescription": "desc",
  "taskStatus": "INPROGRESS",
  "startDate": "2025-01-04",
  "endDate": "2025-01-07"
}
```

### Response

`ApiResponse<Void>`

---

## 4) 태스크 삭제

- Method: `DELETE`
- URL: `/api/projects/{projectId}/tasks/{taskId}`

### Response

`ApiResponse<Void>`

---

## 5) 태스크 완료 처리

- Method: `PATCH`
- URL: `/api/projects/{projectId}/tasks/{taskId}/complete`

### Request Body

```
{
  "completionNote": "완료 사유"
}
```

### Response

`ApiResponse<Void>`

---

## 6) 마일스톤 상세 조회

- Method: `GET`
- URL: `/api/projects/{projectId}/ganttchart/milestones/{milestoneId}`

### Response

`ApiResponse<MilestoneResponse>`

- `milestoneId`: Long
- `projectId`: Long
- `milestoneName`: String
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`
- `startDate`: `YYYY-MM-DD`
- `endDate`: `YYYY-MM-DD`
- `achievementRate`: Long | null
- `isDeleted`: Boolean
- `tasks`: List<TaskResponse>

---

## 7) 마일스톤 목록 조회

- Method: `GET`
- URL: `/api/projects/{projectId}/ganttchart/milestones`

### Response

`ApiResponse<List<MilestoneResponse>>`

---

## 8) 마일스톤 생성

- Method: `POST`
- URL: `/api/projects/{projectId}/ganttchart/milestones`

### Request Body

```
{
  "milestoneName": "M1",
  "startDate": "2025-01-01",
  "endDate": "2025-01-15",
  "achievementRate": 0
}
```

### Response

`ApiResponse<CreatedIdResponse>`

- `id`: Long

---

## 9) 마일스톤 수정

- Method: `PATCH`
- URL: `/api/projects/{projectId}/ganttchart/milestones/{milestoneId}`

### Request Body

```
{
  "milestoneName": "수정된 M1",
  "startDate": "2025-01-02",
  "endDate": "2025-01-16",
  "achievementRate": 20
}
```

### Response

`ApiResponse<Void>`

---

## 10) 마일스톤 삭제

- Method: `DELETE`
- URL: `/api/projects/{projectId}/ganttchart/milestones/{milestoneId}`

### Response

`ApiResponse<Void>`
