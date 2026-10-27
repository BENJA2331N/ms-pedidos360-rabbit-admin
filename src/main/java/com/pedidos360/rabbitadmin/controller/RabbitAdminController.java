package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.BindingAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.ExchangeAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.QueueAdminRequestDTO;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rabbit-admin")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@Valid @RequestBody QueueAdminRequestDTO request) {
        rabbitAdminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cola creada correctamente", "cola", request.getName()));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, String>> deleteQueue(@PathVariable("name") @NotBlank String name) {
        boolean deleted = rabbitAdminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("mensaje", "Cola eliminada correctamente", "cola", name));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró o no se pudo eliminar la cola", "cola", name));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody ExchangeAdminRequestDTO request) {
        rabbitAdminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Exchange creado correctamente", "exchange", request.getName()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, String>> deleteExchange(@PathVariable("name") @NotBlank String name) {
        boolean deleted = rabbitAdminService.deleteExchange(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("mensaje", "Exchange eliminado correctamente", "exchange", name));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró el exchange", "exchange", name));
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(@Valid @RequestBody BindingAdminRequestDTO request) {
        rabbitAdminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Binding creado correctamente"));
    }
}
