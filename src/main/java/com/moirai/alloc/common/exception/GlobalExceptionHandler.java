package com.moirai.alloc.common.exception;

import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * 전역 예외 처리기
 *
 * 목표:
 * - API 응답 포맷(ApiResponse)을 일관되게 유지
 * - Validation / Security / NotFound / Conflict / Unknown을 명확히 분리
 * - ResponseStatusException은 status를 그대로 내려보냄(테스트 404->500 방지)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 NotFound 예외 -> 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return failure(HttpStatus.NOT_FOUND, ex.getErrorCode().name(), ex.getMessage());
    }

    /**
     * 도메인 Forbidden 예외 -> 403
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return failure(HttpStatus.FORBIDDEN, ex.getErrorCode().name(), ex.getMessage());
    }

    /**
     * 서비스/컨트롤러에서 명시적으로 상태코드를 던진 경우 -> 그대로 유지
     * 예: new ResponseStatusException(HttpStatus.NOT_FOUND, "...")
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason()
                : "Request failed";
        // code는 상태 기반으로 통일(필요 시 프로젝트 표준에 맞게 조정)
        String code = status.name();
        return failure(status, code, message);
    }

    /**
     * RequestBody @Valid 검증 실패 -> 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), "Validation failed");
    }

    /**
     * @ModelAttribute, @RequestParam 바인딩 과정의 검증/바인딩 실패 -> 400
     * (스프링이 MethodArgumentNotValidException 대신 BindException을 던지는 케이스 대응)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), "Validation failed");
    }

    /**
     * RequestParam 누락 -> 400
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), "Validation failed");
    }

    /**
     * RequestParam 타입 변환 실패 (예: id=abc) -> 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), "Validation failed");
    }

    /**
     * 잘못된 인자 -> 400
     * - 기존 코드의 "문자열에 NOT_FOUND 포함이면 404" 같은 로직은 불안정하므로 제거 권장
     * - 404가 필요하면 NotFoundException 또는 ResponseStatusException(404) 사용
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(), ex.getMessage());
    }

    /**
     * 상태 충돌(비즈니스 룰) -> 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return failure(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    /**
     * 컨트롤러에서 AccessDeniedException을 던지는 경우(예: principal null 방어) -> 403
     * - Spring Security 필터 단계에서 막히는 케이스는 별도로 401/403 처리될 수 있음
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return failure(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");
    }

    /**
     * 그 외 모든 예외 -> 500
     * - Exception 핸들러는 반드시 "하나만" 유지해야 예측 가능
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception. path={}, method={}, message={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage(),
                ex
        );
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.name(), "Unexpected error");
    }

    /**
     * ApiResponse 실패 응답 생성 헬퍼
     * - 프로젝트 표준에 따라 failure(code,message) 형태로 통일
     */
    private ResponseEntity<ApiResponse<Void>> failure(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(code, message));
    }
}
