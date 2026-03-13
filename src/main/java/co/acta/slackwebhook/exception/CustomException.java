package co.acta.slackwebhook.exception;

import lombok.Getter;

@Getter
public class CustomException extends Exception {
    private ExceptionInfo exceptionInfo;

    public CustomException(ExceptionInfo exceptionInfo) {
        super(exceptionInfo.getMessage());
        this.exceptionInfo = exceptionInfo;
    }
}
