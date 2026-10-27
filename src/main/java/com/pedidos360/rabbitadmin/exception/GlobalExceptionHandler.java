package com.pedidos360.rabbitadmin.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Validación de cuerpos JSON (@Valid) -> 400 con el detalle por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        return badRequest(errores);
    }

    /** Validación de path variables / query params (@NotBlank) -> 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            String campo = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errores.put(campo, v.getMessage());
        });
        return badRequest(errores);
    }

    /** Query param obligatorio ausente -> 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return badRequest(Map.of(ex.getParameterName(), "El parámetro es obligatorio"));
    }

    /** JSON ausente o malformado -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return badRequest(Map.of("body", "El cuerpo de la petición es obligatorio y debe ser un JSON válido"));
    }

    /** RabbitMQ no disponible -> 503. */
    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> handleBrokerDown(AmqpConnectException ex) {
        log.error("No se pudo conectar con RabbitMQ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "RabbitMQ no está disponible");
        body.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    /**
     * Rechazos del broker: recurso inexistente (ej. binding hacia una cola que no existe) -> 404,
     * conflicto de parámetros (cola ya declarada con otra configuración) -> 409, otros -> 502.
     */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleAmqpError(AmqpException ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String detalle = String.valueOf(root.getMessage());
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (detalle.contains("NOT_FOUND")) {
            status = HttpStatus.NOT_FOUND;
        } else if (detalle.contains("PRECONDITION_FAILED")) {
            status = HttpStatus.CONFLICT;
        }
        log.error("RabbitMQ rechazó la operación ({}): {}", status.value(), detalle);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "RabbitMQ rechazó la operación");
        body.put("detalle", detalle);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(Map<String, String> detalles) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Parámetros de entrada inválidos");
        body.put("detalles", detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
