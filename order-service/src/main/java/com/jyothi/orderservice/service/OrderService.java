package com.jyothi.orderservice.service;

import com.jyothi.orderservice.dto.InventoryResponse;
import com.jyothi.orderservice.dto.OrderLineItemsDto;
import com.jyothi.orderservice.dto.OrderRequest;
import com.jyothi.orderservice.event.OrderPlacedEvent;
import com.jyothi.orderservice.model.Order;
import com.jyothi.orderservice.model.OrderLineItems;
import com.jyothi.orderservice.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private KafkaTemplate<String ,OrderPlacedEvent> kafkaTemplate;
    public String  placeOrder(OrderRequest orderRequest){
        Order order=new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems=orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        //Getting list of skuCode from the order object
        List<String> skuCodes=order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode)
                .toList();
        //call inventory service to check if stock is present or not


        InventoryResponse[] inventoryResponsesArray=webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        boolean allProductsInStock=Arrays.stream(inventoryResponsesArray)
                .allMatch(InventoryResponse::isInStock);
        if(allProductsInStock){
            orderRepository.save(order);
//            kafkaTemplate.send("notificationTopic",new OrderPlacedEvent(order.getOrderNumber()));
            return "order placed successfully";
        }
        else{
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems=new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
