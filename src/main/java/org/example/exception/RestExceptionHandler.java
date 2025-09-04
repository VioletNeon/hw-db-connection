package org.example.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail onResponseStatus(ResponseStatusException ex) {
        var pd = ProblemDetail.forStatus(ex.getStatusCode());
        var status = ex.getStatusCode();

        if (status.is4xxClientError()) {
            pd.setTitle(status == HttpStatus.NOT_FOUND ? "Resource not found" : "Request failed");
        } else if (status.is5xxServerError()) {
            pd.setTitle("Payments internal error");
        } else {
            pd.setTitle("Request failed");
        }

        pd.setDetail(ex.getReason());

        return pd;
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail onClientError(HttpClientErrorException ex) {
        var body = ex.getResponseBodyAs(ProblemDetail.class);

        if (body != null) {
            return body;
        }

        var pd = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
        pd.setTitle("Upstream client error");

        return pd;
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail onNetwork(ResourceAccessException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Upstream timeout or network error");
        pd.setTitle("Payments upstream error");

        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation failed");
        pd.setDetail(ex.getMessage());

        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail onConstraintViolation(ConstraintViolationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation failed");
        pd.setDetail(ex.getMessage());

        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail onAny(Exception ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        pd.setTitle("Payments internal error");

        return pd;
    }

}
