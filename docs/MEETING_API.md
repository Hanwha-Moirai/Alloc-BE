# Meeting Record API

본 문서는 회의록(Meeting Record) API 명세를 정리한다.

## 공통

- Base Path: `/api`
- Content-Type: `application/json`
- 응답 포맷: `ApiResponse<T>`
  - 성공: `{"success":true,"data":...,"errorCode":null,"message":null,"timestamp":"..."}`
  - 실패: `{"success":false,"data":null,"errorCode":"CODE","message":"...","timestamp":"..."}`

---

## 1) 회의록 목록 조회 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/meeting_record`
- 설명: 전체 회의록 목록을 페이지 단위로 조회한다.

### Path Parameters

- `projectId`: Long

### Query Parameters

- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<MeetingRecordSummaryResponse>>`

- `meetingId`: Long
- `projectId`: Long
- `projectName`: String
- `createdBy`: String
- `progress`: Double
- `meetingDate`: `YYYY-MM-DDTHH:mm:ss`
- `meetingTime`: `YYYY-MM-DDTHH:mm:ss`
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`

---

## 2) 회의록 검색 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/meeting_record/search`
- 설명: 조건 기반 회의록 검색.

### Path Parameters

- `projectId`: Long

### Query Parameters

- `projectName` (optional)
- `from` (optional) : `YYYY-MM-DD`
- `to` (optional) : `YYYY-MM-DD`
- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<MeetingRecordSummaryResponse>>`

---

## 3) 회의록 상세 조회 (Docs)

- Method: `GET`
- URL: `/api/projects/{projectId}/docs/meeting_record/{meetingRecordId}`

### Path Parameters

- `projectId`: Long
- `meetingRecordId`: Long

### Response

`ApiResponse<MeetingRecordDetailResponse>`

- `meetingId`: Long
- `projectId`: Long
- `createdBy`: String
- `progress`: Double
- `meetingDate`: `YYYY-MM-DDTHH:mm:ss`
- `meetingTime`: `YYYY-MM-DDTHH:mm:ss`
- `createdAt`: `YYYY-MM-DDTHH:mm:ss`
- `updatedAt`: `YYYY-MM-DDTHH:mm:ss`
- `agendas`: List
  - `agendaId`: Long
  - `discussionTitle`: String
  - `discussionContent`: String | null
  - `discussionResult`: String | null
  - `agendaType`: String | null
- `participants`: List
  - `userId`: Long
  - `isHost`: Boolean

---

## 4) 회의록 생성 (Docs)

- Method: `POST`
- URL: `/api/projects/{projectId}/docs/meeting_record/create`
- 설명: 회의록을 생성한다.

### Path Parameters

- `projectId`: Long

### Request Body

```
{
  "projectId": 88001,
  "progress": 30.0,
  "meetingDate": "2025-01-10T10:00:00",
  "meetingTime": "2025-01-10T10:00:00",
  "agendas": [
    {
      "discussionTitle": "title",
      "discussionContent": "content",
      "discussionResult": "result",
      "agendaType": "TYPE1"
    }
  ],
  "participants": [
    {
      "userId": 88002,
      "isHost": false
    }
  ]
}
```

### Response

`ApiResponse<Long>`

---

## 5) 회의록 수정 (Docs)

- Method: `PATCH`
- URL: `/api/projects/{projectId}/docs/meeting_record/save`
- 설명: 회의록 내용을 수정한다.

### Path Parameters

- `projectId`: Long

### Request Body

```
{
  "meetingId": 88001,
  "progress": 50.0,
  "meetingDate": "2025-01-12T10:00:00",
  "meetingTime": "2025-01-12T10:00:00",
  "agendas": [
    {
      "discussionTitle": "updated",
      "discussionContent": "content",
      "discussionResult": "result",
      "agendaType": "TYPE2"
    }
  ],
  "participants": [
    {
      "userId": 88001,
      "isHost": true
    }
  ]
}
```

### Response

`ApiResponse<Void>`

---

## 6) 회의록 삭제 (Docs)

- Method: `DELETE`
- URL: `/api/projects/{projectId}/docs/meeting_record/delete`
- 설명: 회의록을 소프트 삭제한다.

### Path Parameters

- `projectId`: Long

### Query Parameter

- `meetingId`: Long

### Response

`ApiResponse<Void>`

---

## 7) 내 프로젝트 회의록 목록 조회 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/meeting_record`

### Query Parameters

- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<MeetingRecordSummaryResponse>>`

---

## 8) 내 프로젝트 회의록 검색 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/meeting_record/search`

### Query Parameters

- `projectName` (optional)
- `from` (optional)
- `to` (optional)
- `page` (optional)
- `size` (optional)

### Response

`ApiResponse<Page<MeetingRecordSummaryResponse>>`

---

## 9) 내 프로젝트 회의록 상세 조회 (MyDocs)

- Method: `GET`
- URL: `/api/mydocs/meeting_record/{meetingRecordId}`

### Response

`ApiResponse<MeetingRecordDetailResponse>`
