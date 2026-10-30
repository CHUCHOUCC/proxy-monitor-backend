package com.ucc.proxymonitor.web;

import com.ucc.proxymonitor.exception.BusinessRuleException;
import com.ucc.proxymonitor.exception.InvalidArgumentsException;
import com.ucc.proxymonitor.exception.PaymentGatewayException;
import com.ucc.proxymonitor.exception.ResourceNotFoundException;
import com.ucc.proxymonitor.exception.ServiceExecutionException;
import com.ucc.proxymonitor.exception.UnknownOperationException;
import com.ucc.proxymonitor.exception.UnknownServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Translates exceptions into JSON error responses with the right HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceExecutionException.class)
    public ResponseEntity<ErrorResponse> handleServiceError(ServiceExecutionException ex) {
        HttpStatus status = statusFor(ex.getCause());
        return build(status, ex.getMessage(), ex.getRequestId(), ex.getServiceId(), ex.getOperation());
    }

    @ExceptionHandler(InvalidArgumentsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArguments(InvalidArgumentsException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null, null);
    }

    @ExceptionHandler(UnknownServiceException.class)
    public ResponseEntity<ErrorResponse> handleUnknownService(UnknownServiceException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null, null, null);
    }

    private HttpStatus statusFor(Throwable cause) {
        if (cause instanceof InvalidArgumentsException) return HttpStatus.BAD_REQUEST;
        if (cause instanceof UnknownOperationException) return HttpStatus.NOT_FOUND;
        if (cause instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (cause instanceof BusinessRuleException) return HttpStatus.CONFLICT;
        if (cause instanceof PaymentGatewayException) return HttpStatus.SERVICE_UNAVAILABLE;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                String requestId, String serviceId, String operation) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                message, requestId, serviceId, operation);
        return ResponseEntity.status(status).body(body);
    }
}
