package com.khanabook.pos.service.impl;

import com.khanabook.pos.dto.response.KitchenOrderResponse;
import com.khanabook.pos.exception.InvalidOrderStateException;
import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.repository.CustomerOrderRepository;
import com.khanabook.pos.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {

    private final CustomerOrderRepository orderRepository;

    @Override
    @Transactional
    public List<KitchenOrderResponse> getPendingOrders() {
        List<CustomerOrder> orders = orderRepository.findByStatusInWithItems(
                Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.IN_KITCHEN));

        return orders.stream()
                .map(this::convertToKitchenResponse)
                .sorted(Comparator.comparing(KitchenOrderResponse::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<KitchenOrderResponse> getActiveOrders() {
        List<CustomerOrder> orders = orderRepository.findByStatusInWithItems(
                Arrays.asList(OrderStatus.IN_KITCHEN, OrderStatus.READY_TO_SERVE));

        return orders.stream()
                .map(this::convertToKitchenResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerOrder markOrderReady(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.IN_KITCHEN) {
            throw new InvalidOrderStateException("Order is not in kitchen");
        }

        order.setStatus(OrderStatus.READY_TO_SERVE);
        order.setReadyAt(LocalDateTime.now());

        // Calculate actual KPT
        if (order.getSentToKitchenAt() != null) {
            long actualMinutes = ChronoUnit.MINUTES.between(
                    order.getSentToKitchenAt(),
                    order.getReadyAt());
            order.setActualKptMinutes((int) actualMinutes);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public CustomerOrder updateKpt(Long orderId, Integer estimatedMinutes) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (estimatedMinutes < 1 || estimatedMinutes > 30) {
            throw new IllegalArgumentException("KPT must be between 1 and 30 minutes");
        }

        order.setEstimatedKptMinutes(estimatedMinutes);
        order.calculateEstimatedReadyTime();

        return orderRepository.save(order);
    }

    private KitchenOrderResponse convertToKitchenResponse(CustomerOrder order) {
        KitchenOrderResponse response = new KitchenOrderResponse();
        response.setOrderId(order.getId());
        response.setTableName(
                order.getRestaurantTable() != null ? order.getRestaurantTable().getName() : "Takeaway/Delivery");
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setSentToKitchenAt(order.getSentToKitchenAt());
        response.setEstimatedKptMinutes(order.getEstimatedKptMinutes());
        response.setEstimatedReadyTime(order.getEstimatedReadyTime());
        response.setSpecialInstructions(order.getSpecialInstructions());

        List<KitchenOrderResponse.KitchenItemDto> items = order.getOrderItems().stream()
                .map(item -> {
                    KitchenOrderResponse.KitchenItemDto dto = new KitchenOrderResponse.KitchenItemDto();
                    dto.setMenuItemName(item.getMenuItem().getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setSpecialInstructions(item.getSpecialInstructions());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setItems(items);
        return response;
    }
}
