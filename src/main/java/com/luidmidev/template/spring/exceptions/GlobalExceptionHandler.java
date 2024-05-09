package com.luidmidev.template.spring.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Function<ConstraintViolation<?>, ErrorResponse> constraintViolationToER = constraintViolation -> {
        var path = constraintViolation.getPropertyPath().toString().split("\\.");
        var param = path[path.length - 1];
        return ErrorResponse.of(constraintViolation.getMessage(), param);
    };

    private static final Function<ObjectError, ErrorResponse> objectErrorErrorToER = error -> {
        if (error instanceof FieldError errField)
            return ErrorResponse.of(errField.getDefaultMessage(), errField.getField());
        else {
            return ErrorResponse.of(error.getDefaultMessage());
        }
    };


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Se recibió una excepción de AuthenticationException: {}", ex.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorResponse>> handleConstraintViolationException(ConstraintViolationException ex) {
        var constraintViolations = ex.getConstraintViolations();
        var errors = constraintViolations
                .stream()
                .map(constraintViolationToER)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String errorMessage = "Error: El parámetro requerido '" + ex.getParameterName() + "' no está presente en la solicitud.";
        return ResponseEntity.badRequest().body(ErrorResponse.of(errorMessage, ex.getParameterName()));
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ex) {
        return ResponseEntity.status(ex.getHttpStatusCode()).body(ex.getErrorResponse());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {

        List<ErrorResponse> errors = ex
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(objectErrorErrorToER)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException ex) {
        String key = ex.getProblem().replace("found duplicate key ", "");
        return onDuplicateKey(key);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.warn("Se recibió una excepción de SQLIntegrityConstraintViolationException, se recomienda implementar el método onSQLIntegrityConstraintViolationException de la clase   GlobalExcepcionHandler para evitar fugas de información del esquema de base de datos: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of("Error en la consistencia de datos"));
    }

    public ResponseEntity<ErrorResponse> onDuplicateKey(String key) {

        String message;
        if (key.contains("username")) message = "El usuario con ese nombre de usuario ya existe";
        else if (key.contains("email")) message = "El usuario con ese correo electrónico ya existe";
        else message = "Error de duplicación de clave";

        return ResponseEntity.badRequest().body(ErrorResponse.of(message));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotWritableException() {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Error al escribir la respuesta"));
    }


}