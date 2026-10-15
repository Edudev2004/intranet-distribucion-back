package com.dwi.intranetdistribucion.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> manejarResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest) {
        return construirRespuesta(exception.getMessage(), webRequest, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorDetails> manejarDuplicateResourceException(DuplicateResourceException exception, WebRequest webRequest) {
        return construirRespuesta(exception.getMessage(), webRequest, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<ErrorDetails> manejarResourceInUseException(ResourceInUseException exception, WebRequest webRequest) {
        return construirRespuesta(exception.getMessage(), webRequest, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> manejarBadRequestException(BadRequestException exception, WebRequest webRequest) {
        return construirRespuesta(exception.getMessage(), webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> manejarValidacion(MethodArgumentNotValidException exception, WebRequest webRequest) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construirRespuesta(mensaje, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> manejarCuerpoInvalido(HttpMessageNotReadableException exception, WebRequest webRequest) {
        return construirRespuesta("El cuerpo de la petición no es válido", webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> manejarIntegridad(DataIntegrityViolationException exception, WebRequest webRequest) {
        return construirRespuesta("La operación viola una restricción de integridad de datos", webRequest, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> manejarGlobalException(Exception exception, WebRequest webRequest) {
        return construirRespuesta(exception.getMessage(), webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDetails> construirRespuesta(String mensaje, WebRequest webRequest, HttpStatus status) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                mensaje,
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, status);
    }
}
