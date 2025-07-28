package org.mi.plannitybe.exception.handler;

import org.mi.plannitybe.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    // TODO) GlobalExceptionHandler 리팩토링하기

    // 클라이언트 요청 데이터 유효성 검사 실패 처리 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        // 모든 에러 메시지 List에 저장
        List<String> messages = new ArrayList<>();
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        for (ObjectError error : errors) {
            String message = error.getDefaultMessage();
            messages.add(message);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "code", "INVALID_ARGUMENT",
                        "messages", messages
                ));
    }

    // 클라이언트 요청 데이터 형식 오류 처리 (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause().getCause();
        if (cause instanceof IllegalArgumentException) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // 400
                    .body(Map.of(
                            "code", "INVALID_ARGUMENT",
                            "messages", cause.getMessage()
                    ));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "code", "WRONG_DATA_FORMAT",
                        "messages", "요청한 데이터의 형식이 올바르지 않습니다."
                ));
    }

    // 종일일정일 때 일정 시작날짜와 종료날짜가 유효하지 않은 경우 예외 처리
    @ExceptionHandler(InvalidAllDayEventDateException.class)
    public ResponseEntity<?> handleInvalidAllDayEventDateException(InvalidAllDayEventDateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "code", "INVALID_ARGUMENT",
                        "messages", ex.getMessage()
                ));
    }

    // 이미 존재하는 이메일로 회원가입 시도하여 실패할 때 예외 처리 (409)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "code", "EMAIL_ALREADY_EXISTS",
                        "message", ex.getMessage()
                ));
    }

    // 클라이언트 요청값에 해당하는 데이터가 존재하지 않을 때 예외 처리 (404)
    @ExceptionHandler({EventListNotFoundException.class, TaskNotFoundException.class})
    public ResponseEntity<?> handleNotFoundException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(Map.of(
                        "code", "DATA_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    // 요청한 EventList의 소유자가 아니어서 접근 권한이 없을 때 예외 처리 (403)
    @ExceptionHandler(ForbiddenEventListAccessException.class)
    public ResponseEntity<?> handleForbiddenEventListAccessException(ForbiddenEventListAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 403
                .body(Map.of(
                        "code", "FORBIDDEN_EVENT_LIST",
                        "message", ex.getMessage()
                ));
    }

    // 로그인 실패 시 spring security 예외 처리 (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "code", "INVALID_EMAIL_OR_PASSWORD",
                        "message", "아이디 또는 비밀번호가 올바르지 않습니다."
                ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "code", "DISABLED_ACCOUNT",
                        "message", "계정이 비활성화 상태입니다. 관리자에게 문의하세요."
                ));
    }

    // internal server error (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "code", "SERVER_ERROR",
                        "error", ex.getClass().getName(),
                        "message", ex.getMessage()
                ));
    }
}
