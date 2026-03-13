package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException e) {
        log.error("[CustomException] code={}, message={}", e.getExceptionInfo().getErrorCode(), e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", e.getExceptionInfo().getErrorCode());
        body.put("message", e.getExceptionInfo().getMessage());
        return ResponseEntity
                .status(e.getExceptionInfo().getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", 500);
        body.put("message", "서버 내부 오류가 발생하였습니다.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
