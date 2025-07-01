package org.mi.plannitybe.exception.handler;

import org.mi.plannitybe.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "code", "EMAIL_ALREADY_EXISTS",
                        "message", ex.getMessage()
                ));
    }

    // 로그인 실패 시 spring security 예외 처리
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

    // internal server error 500 처리
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
