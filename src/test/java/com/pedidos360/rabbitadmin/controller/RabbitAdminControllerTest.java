package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.exception.GlobalExceptionHandler;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.ConnectException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RabbitAdminController.class)
@Import(GlobalExceptionHandler.class)
class RabbitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitAdminService service;

    @Test
    @DisplayName("POST /queues con nombre válido -> 201")
    void createQueueOk() throws Exception {
        mockMvc.perform(post("/api/rabbit-admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"cola.test\",\"durable\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cola").value("cola.test"));
    }

    @Test
    @DisplayName("POST /queues con nombre vacío -> 400 con mensaje claro y sin llamar al servicio")
    void createQueueBlankName() throws Exception {
        mockMvc.perform(post("/api/rabbit-admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.name").exists());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /queues con prefijo reservado amq. -> 400")
    void createQueueReservedPrefix() throws Exception {
        mockMvc.perform(post("/api/rabbit-admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"amq.mia\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /exchanges con tipo inválido -> 400")
    void createExchangeInvalidType() throws Exception {
        mockMvc.perform(post("/api/rabbit-admin/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ex.test\",\"type\":\"headers\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.type").exists());
    }

    @Test
    @DisplayName("POST /bindings con campos vacíos -> 400")
    void createBindingInvalid() throws Exception {
        mockMvc.perform(post("/api/rabbit-admin/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"queueName\":\"\",\"exchangeName\":\"\",\"routingKey\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.queueName").exists())
                .andExpect(jsonPath("$.detalles.exchangeName").exists())
                .andExpect(jsonPath("$.detalles.routingKey").exists());
    }

    @Test
    @DisplayName("DELETE /queues/{name} existente -> 200; inexistente -> 404")
    void deleteQueue() throws Exception {
        when(service.deleteQueue("existe")).thenReturn(true);
        when(service.deleteQueue("noexiste")).thenReturn(false);

        mockMvc.perform(delete("/api/rabbit-admin/queues/existe")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/rabbit-admin/queues/noexiste")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /exchanges/{name} existente -> 200; inexistente -> 404")
    void deleteExchange() throws Exception {
        when(service.deleteExchange("existe")).thenReturn(true);
        when(service.deleteExchange("noexiste")).thenReturn(false);

        mockMvc.perform(delete("/api/rabbit-admin/exchanges/existe")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/rabbit-admin/exchanges/noexiste")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /bindings con query params -> 200; sin params -> 400")
    void deleteBinding() throws Exception {
        mockMvc.perform(delete("/api/rabbit-admin/bindings")
                        .param("queueName", "q").param("exchangeName", "e").param("routingKey", "rk"))
                .andExpect(status().isOk());
        verify(service).deleteBinding("q", "e", "rk");

        mockMvc.perform(delete("/api/rabbit-admin/bindings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RabbitMQ caído -> 503 Service Unavailable")
    void brokerDown() throws Exception {
        doThrow(new AmqpConnectException(new ConnectException("refused"))).when(service).createQueue(any());

        mockMvc.perform(post("/api/rabbit-admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"cola.test\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
