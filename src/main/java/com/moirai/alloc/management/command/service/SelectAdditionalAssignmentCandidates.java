package com.moirai.alloc.management.command.service;

public class SelectAdditionalAssignmentCandidates {
    public void selectAdditionalCandidates(Long projectId) {

        // 1) 프로젝트 조회
        // 2) 현재 ASSIGNED / PENDING 후보 수 조회
        // 3) 필요한 인원 수 계산
        // 4) 이미 제외된 인원 목록 조회
        // 5) policy로 추가 후보 계산
        // 6) SquadAssignment.propose()로 추가 생성
        // 7) 저장
    }
}
