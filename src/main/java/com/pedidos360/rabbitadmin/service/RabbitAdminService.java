package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.BindingAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.ExchangeAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.QueueAdminRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

/**
 * Encapsula toda la lógica de administración de RabbitMQ (colas, exchanges y bindings).
 * Los controladores solo invocan estos métodos de alto nivel y no conocen la API de AMQP.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    /** Declara (crea) una cola. Es idempotente si ya existe con los mismos parámetros. */
    public void createQueue(QueueAdminRequestDTO dto) {
        Queue queue = new Queue(dto.getName().trim(), dto.isDurable());
        amqpAdmin.declareQueue(queue);
        log.info("Cola declarada: {} (durable={})", queue.getName(), queue.isDurable());
    }

    /** Elimina una cola. @return true si RabbitMQ confirmó la eliminación. */
    public boolean deleteQueue(String name) {
        boolean deleted = amqpAdmin.deleteQueue(name.trim());
        log.info("Eliminación de cola {}: {}", name.trim(), deleted);
        return deleted;
    }

    /** Declara (crea) un exchange durable de tipo direct, topic o fanout. */
    public void createExchange(ExchangeAdminRequestDTO dto) {
        String name = dto.getName().trim();
        Exchange exchange = switch (dto.getType()) {
            case "topic" -> new TopicExchange(name, true, false);
            case "fanout" -> new FanoutExchange(name, true, false);
            default -> new DirectExchange(name, true, false);
        };
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange declarado: {} (tipo={})", name, dto.getType());
    }

    /** Elimina un exchange. @return true si RabbitMQ confirmó la eliminación. */
    public boolean deleteExchange(String name) {
        boolean deleted = amqpAdmin.deleteExchange(name.trim());
        log.info("Eliminación de exchange {}: {}", name.trim(), deleted);
        return deleted;
    }

    /** Crea el binding cola -> exchange con el routingKey indicado. */
    public void createBinding(BindingAdminRequestDTO dto) {
        amqpAdmin.declareBinding(toBinding(dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey()));
        log.info("Binding creado: cola={} exchange={} routingKey={}",
                dto.getQueueName().trim(), dto.getExchangeName().trim(), dto.getRoutingKey().trim());
    }

    /** Elimina el binding cola -> exchange con el routingKey indicado. */
    public void deleteBinding(String queueName, String exchangeName, String routingKey) {
        amqpAdmin.removeBinding(toBinding(queueName, exchangeName, routingKey));
        log.info("Binding eliminado: cola={} exchange={} routingKey={}",
                queueName.trim(), exchangeName.trim(), routingKey.trim());
    }

    private Binding toBinding(String queueName, String exchangeName, String routingKey) {
        return new Binding(
                queueName.trim(),
                Binding.DestinationType.QUEUE,
                exchangeName.trim(),
                routingKey.trim(),
                null
        );
    }
}
