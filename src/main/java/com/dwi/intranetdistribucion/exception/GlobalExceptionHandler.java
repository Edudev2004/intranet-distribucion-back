package com.dwi.intranetdistribucion.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
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
        log.warn("Violación de integridad de datos en [{}]: {}", webRequest.getDescription(false), exception.getMessage());
        return construirRespuesta("La operación viola una restricción de integridad de datos", webRequest, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDetails> manejarDataAccessException(DataAccessException exception, WebRequest webRequest) {
        log.error("Error de base de datos o consulta SQL en [{}]: ", webRequest.getDescription(false), exception);
        return construirRespuesta("No fue posible completar la operación en el sistema. Por favor, comuníquese con el administrador.", webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> manejarBadCredentialsException(BadCredentialsException exception, WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Usuario o contraseña incorrectos",
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> manejarAccessDeniedException(AccessDeniedException exception, WebRequest webRequest) {
        return construirRespuesta("No cuenta con permisos suficientes para realizar esta acción.", webRequest, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDetails> manejarDisabledException(DisabledException exception, WebRequest webRequest) {
        return construirRespuesta("La cuenta está desactivada. Contacte al administrador.", webRequest, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorDetails> manejarLockedException(LockedException exception, WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.LOCKED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> manejarIllegalArgumentException(IllegalArgumentException exception, WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetails> manejarIllegalStateException(IllegalStateException exception, WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> manejarGlobalException(Exception exception, WebRequest webRequest) {
        log.error("Excepción no controlada en el servidor en [{}]: ", webRequest.getDescription(false), exception);
        return construirRespuesta("Ha ocurrido un error inesperado en el servidor. Por favor, intente más tarde.", webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
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
