package jp.yoshiaki.insuranceapp.web.advice;

import jp.yoshiaki.insuranceapp.domain.exception.DomainException;
import jp.yoshiaki.insuranceapp.domain.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.domain.exception.ValidationException;
import jp.yoshiaki.insuranceapp.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(ex.getCode(), ex.getMessage()));
    }

    // いったん最小：DomainExceptionをまとめて受けたい場合の保険
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(ex.getCode(), ex.getMessage()));
    }
}
