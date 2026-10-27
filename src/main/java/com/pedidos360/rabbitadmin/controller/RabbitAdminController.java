package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.BindingAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.ExchangeAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.QueueAdminRequestDTO;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API REST de administración de RabbitMQ.
 * <pre>
 *  POST   /api/rabbit-admin/queues            -> crear cola          (201 | 400)
 *  DELETE /api/rabbit-admin/queues/{name}     -> eliminar cola       (200 | 404)
 *  POST   /api/rabbit-admin/exchanges         -> crear exchange      (201 | 400)
 *  DELETE /api/rabbit-admin/exchanges/{name}  -> eliminar exchange   (200 | 404)
 *  POST   /api/rabbit-admin/bindings          -> crear binding       (201 | 400)
 *  DELETE /api/rabbit-admin/bindings?queueName=&amp;exchangeName=&amp;routingKey= -> eliminar binding (200 | 400)
 * </pre>
 * Documentación interactiva (OpenAPI): /swagger-ui.html
 */
@Validated
@RestController
@RequestMapping("/api/rabbit-admin")
@Tag(name = "RabbitMQ Admin", description = "Creación y eliminación de colas, exchanges y bindings")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @Operation(summary = "Crear una cola", description = "Declara una cola (durable por defecto).")
    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@Valid @RequestBody QueueAdminRequestDTO request) {
        rabbitAdminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cola creada correctamente", "cola", request.getName()));
    }

    @Operation(summary = "Eliminar una cola por nombre")
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, String>> deleteQueue(@PathVariable("name") @NotBlank String name) {
        boolean deleted = rabbitAdminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("mensaje", "Cola eliminada correctamente", "cola", name));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró o no se pudo eliminar la cola", "cola", name));
    }

    @Operation(summary = "Crear un exchange", description = "Tipos permitidos: direct, topic y fanout.")
    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody ExchangeAdminRequestDTO request) {
        rabbitAdminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Exchange creado correctamente", "exchange", request.getName()));
    }

    @Operation(summary = "Eliminar un exchange por nombre")
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, String>> deleteExchange(@PathVariable("name") @NotBlank String name) {
        boolean deleted = rabbitAdminService.deleteExchange(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("mensaje", "Exchange eliminado correctamente", "exchange", name));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró el exchange", "exchange", name));
    }

    @Operation(summary = "Crear un binding", description = "Enlaza una cola existente con un exchange existente.")
    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(@Valid @RequestBody BindingAdminRequestDTO request) {
        rabbitAdminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Binding creado correctamente"));
    }

    @Operation(summary = "Eliminar un binding",
            description = "Identifica el binding por cola, exchange y routingKey (query params).")
    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, String>> deleteBinding(
            @RequestParam("queueName") @NotBlank(message = "El nombre de la cola no puede estar vacío") String queueName,
            @RequestParam("exchangeName") @NotBlank(message = "El nombre del exchange no puede estar vacío") String exchangeName,
            @RequestParam("routingKey") @NotBlank(message = "El routingKey no puede estar vacío") String routingKey) {
        rabbitAdminService.deleteBinding(queueName, exchangeName, routingKey);
        return ResponseEntity.ok(Map.of("mensaje", "Binding eliminado correctamente"));
    }
}
