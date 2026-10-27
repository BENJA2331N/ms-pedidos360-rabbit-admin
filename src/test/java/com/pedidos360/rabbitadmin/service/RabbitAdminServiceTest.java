package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.BindingAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.ExchangeAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.QueueAdminRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private RabbitAdminService service;

    @Test
    @DisplayName("Crear cola: declara una Queue con el nombre (sin espacios) y durabilidad indicada")
    void createQueue() {
        QueueAdminRequestDTO dto = new QueueAdminRequestDTO();
        dto.setName("  cola.test  ");
        dto.setDurable(false);

        service.createQueue(dto);

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        assertEquals("cola.test", captor.getValue().getName());
        assertFalse(captor.getValue().isDurable());
    }

    @Test
    @DisplayName("Eliminar cola: delega en AmqpAdmin y retorna su resultado")
    void deleteQueue() {
        when(amqpAdmin.deleteQueue("cola.test")).thenReturn(true);
        assertTrue(service.deleteQueue(" cola.test "));
    }

    @Test
    @DisplayName("Crear exchange: el tipo determina la clase declarada")
    void createExchangeByType() {
        for (String[] caso : new String[][]{{"direct", "DirectExchange"}, {"topic", "TopicExchange"}, {"fanout", "FanoutExchange"}}) {
            ExchangeAdminRequestDTO dto = new ExchangeAdminRequestDTO();
            dto.setName("ex." + caso[0]);
            dto.setType(caso[0]);
            service.createExchange(dto);
        }

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(amqpAdmin, times(3)).declareExchange(captor.capture());
        assertInstanceOf(DirectExchange.class, captor.getAllValues().get(0));
        assertInstanceOf(TopicExchange.class, captor.getAllValues().get(1));
        assertInstanceOf(FanoutExchange.class, captor.getAllValues().get(2));
    }

    @Test
    @DisplayName("Eliminar exchange: delega en AmqpAdmin")
    void deleteExchange() {
        when(amqpAdmin.deleteExchange("ex.test")).thenReturn(false);
        assertFalse(service.deleteExchange("ex.test"));
    }

    @Test
    @DisplayName("Crear binding: declara el Binding cola -> exchange con el routingKey")
    void createBinding() {
        BindingAdminRequestDTO dto = new BindingAdminRequestDTO();
        dto.setQueueName("cola.test");
        dto.setExchangeName("ex.test");
        dto.setRoutingKey("rk.test");

        service.createBinding(dto);

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).declareBinding(captor.capture());
        assertEquals("cola.test", captor.getValue().getDestination());
        assertEquals("ex.test", captor.getValue().getExchange());
        assertEquals("rk.test", captor.getValue().getRoutingKey());
    }

    @Test
    @DisplayName("Eliminar binding: remueve el Binding identificado por cola, exchange y routingKey")
    void deleteBinding() {
        service.deleteBinding("cola.test", "ex.test", "rk.test");

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).removeBinding(captor.capture());
        assertEquals("cola.test", captor.getValue().getDestination());
        assertEquals("rk.test", captor.getValue().getRoutingKey());
    }
}
