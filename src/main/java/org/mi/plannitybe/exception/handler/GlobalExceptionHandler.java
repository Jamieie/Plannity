package org.mi.plannitybe.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.mi.plannitybe.exception.*;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    // 필수 요청 파라미터 누락 처리 (400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "MISSING_PARAMETER",
                        "message", String.format("필수 파라미터 '%s'가 누락되었습니다.", ex.getParameterName())
                ));
    }

    // 요청 파라미터의 타입 변환 실패 (PathVariable, RequestParam 등) 처리 (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "INVALID_PARAMETER_TYPE",
                        "message", "요청 파라미터 타입이 올바르지 않습니다."
                ));
    }

    // 요청 파라미터 유효성 검사 실패 처리 (400)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerValidation(HandlerMethodValidationException ex) {

        List<Map<String, String>> fieldErrors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getValueResults()) {
            String parameterName = result.getMethodParameter().getParameterName();  // 유효성 검사 실패한 파라미터
            result.getResolvableErrors().forEach(error ->
                    fieldErrors.add(Map.of(
                            "field", parameterName,
                            "message", error.getDefaultMessage()
                    ))
            );
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "code", "VALIDATION_FAILED",
                        "fieldErrors", fieldErrors
                ));
    }

    // 클라이언트 요청 데이터 형식 오류 처리 (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "INVALID_REQUEST_BODY",
                        "message", "요청 본문(JSON) 형식이 올바르지 않습니다."
                ));
    }

    // 클라이언트 요청 데이터 유효성 검사 실패 처리 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<Map<String, String>> fieldErrors = new ArrayList<>();
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        for (ObjectError error : errors) {
            String field = error instanceof org.springframework.validation.FieldError 
                    ? ((org.springframework.validation.FieldError) error).getField() 
                    : "unknown";
            String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "유효성 검사 실패";
            
            // 날짜 형식 오류 메시지 가공
            if (message.contains("Failed to convert") && message.contains("LocalDateTime")) {
                message = field + " 필드의 날짜 형식이 올바르지 않습니다. (예: 2024-04-01T10:00:00)";
            }
            
            fieldErrors.add(Map.of(
                    "field", field,
                    "message", message
            ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "code", "VALIDATION_FAILED",
                        "fieldErrors", fieldErrors
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
    // 본인 소유가 아닌 데이터에 접근 시도할 때 예외 처리 (404 : 보안을 위해 403 X)
    @ExceptionHandler({EventListNotFoundException.class, TaskNotFoundException.class,
            EventNotFoundException.class, EventAccessDeniedException.class,
            EventListAccessDeniedException.class, TaskAccessDeniedException.class})
    public ResponseEntity<?> handleNotFoundException(ResourceException ex) {

        if (ex instanceof ResourceAccessDeniedException) {        // 권한 없는 접근 시도 로그
            log.warn("[AccessDenied] {} | userId={} resourceType={} resourceId={} errorType=AccessDenied",
                    ex.getMessage(), ex.getUserId(), ex.getResourceType(), ex.getResourceId());
        } else if (ex instanceof ResourceNotFoundException) {        // 존재하지 않는 리소스 접근 시도 로그
            log.info("[NotFound] {} | userId={} resourceType={} resourceId={} errorType=NotFound",
                    ex.getMessage(), ex.getUserId(), ex.getResourceType(), ex.getResourceId());
        }

        String code;
        String message;

        if (ex instanceof EventNotFoundException || ex instanceof EventAccessDeniedException) {
            code = "EVENT_NOT_FOUND";
            message = "일정이 존재하지 않습니다.";
        } else if (ex instanceof EventListNotFoundException || ex instanceof EventListAccessDeniedException) {
            code = "EVENT_LIST_NOT_FOUND";
            message = "일정리스트가 존재하지 않습니다.";
        } else if (ex instanceof TaskNotFoundException || ex instanceof TaskAccessDeniedException) {
            code = "TASK_NOT_FOUND";
            message = "할일이 존재하지 않습니다.";
        } else {
            code = "RESOURCE_NOT_FOUND";
            message = "리소스가 존재하지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", code, "message", message));
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

    // 잘못된 URL 요청 시 예외 처리
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "code", "INVALID_URI",
                        "message", "요청하신 URI가 올바르지 않습니다.",
                        "requestURI", request.getRequestURI()
                ));
    }

    // internal server error (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        log.error("Unexpected server error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "code", "SERVER_ERROR",
                        "message", "서버 내부 오류가 발생했습니다."
                ));
    }
}
