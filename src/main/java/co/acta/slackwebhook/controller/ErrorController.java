package co.acta.slackwebhook.controller;

import co.acta.slackwebhook.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleException(CustomException customException) {
        return ResponseEntity.status(customException.getExceptionInfo().getErrorCode()).body(customException.getExceptionInfo().getMessage());
    }
}
