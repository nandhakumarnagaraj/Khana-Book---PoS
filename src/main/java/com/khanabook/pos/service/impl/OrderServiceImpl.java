package com.khanabook.pos.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khanabook.pos.dto.request.OrderItemRequest;
import com.khanabook.pos.dto.request.OrderRequest;
import com.khanabook.pos.dto.request.QrOrderRequest;
import com.khanabook.pos.dto.response.OrderResponse;
import com.khanabook.pos.exception.InvalidOrderStateException;
import com.khanabook.pos.exception.OrderNotEditableException;
import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.KitchenPreparationTime;
import com.khanabook.pos.model.MenuItem;
import com.khanabook.pos.model.OrderItem;
import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.model.OrderType;
import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;
import com.khanabook.pos.model.User;
import com.khanabook.pos.repository.CustomerOrderRepository;
import com.khanabook.pos.repository.KptRepository;
import com.khanabook.pos.repository.MenuItemRepository;
import com.khanabook.pos.repository.RestaurantTableRepository;
import com.khanabook.pos.service.OrderService;
import com.khanabook.pos.service.WhatsAppService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CustomerOrderRepository orderRepository;
	private final MenuItemRepository menuItemRepository;
	private final RestaurantTableRepository tableRepository;
	private final KptRepository kptRepository;
	private final WhatsAppService whatsAppService;

	@Override
	@Transactional
	public OrderResponse createOrder(OrderRequest request) {
		CustomerOrder order = new CustomerOrder();
		order.setOrderType(request.getOrderType());
		order.setStatus(OrderStatus.PENDING);
		order.setIsQrOrder(false);
		order.setIsEditable(true);
		order.setSpecialInstructions(request.getSpecialInstructions());
		order.setCustomerPhone(request.getCustomerPhone());

		// Set table for DINE_IN orders
		if (request.getOrderType() == OrderType.DINE_IN) {
			if (request.getTableId() == null) {
				throw new IllegalArgumentException("Table ID is required for dine-in orders");
			}
			RestaurantTable table = tableRepository.findById(request.getTableId())
					.orElseThrow(() -> new ResourceNotFoundException("Table not found"));
			order.setRestaurantTable(table);
			table.setStatus(TableStatus.OCCUPIED);
			tableRepository.save(table);
		}

		// Set created by user
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof User) {
			order.setCreatedBy((User) auth.getPrincipal());
		}

		// Add order items
		List<OrderItem> orderItems = createOrderItems(request.getItems(), order);
		order.setOrderItems(orderItems);

		// Calculate total
		double total = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
		order.setTotalAmount(total);

		// Calculate estimated KPT
		calculateEstimatedKpt(order);

		order = orderRepository.save(order);

		return convertToResponse(order);
	}

	@Override
	@Transactional
	public OrderResponse createQrOrder(QrOrderRequest request) {
		// Find table by QR token
		RestaurantTable table = tableRepository.findByQrToken(request.getQrToken())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid QR code"));

		CustomerOrder order = new CustomerOrder();
		order.setOrderType(OrderType.DINE_IN);
		order.setStatus(OrderStatus.PENDING);
		order.setRestaurantTable(table);
		order.setIsQrOrder(true);
		order.setIsEditable(true);
		order.setEditableUntil(LocalDateTime.now().plusMinutes(2)); // 2-minute edit window
		order.setSpecialInstructions(request.getSpecialInstructions());
		order.setCustomerPhone(request.getCustomerPhone());

		// Update table status
		table.setStatus(TableStatus.OCCUPIED);
		tableRepository.save(table);

		// Add order items
		List<OrderItem> orderItems = createOrderItems(request.getItems(), order);
		order.setOrderItems(orderItems);

		// Calculate total
		double total = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
		order.setTotalAmount(total);

		// Calculate estimated KPT
		calculateEstimatedKpt(order);

		order = orderRepository.save(order);

		return convertToResponse(order);
	}

	@Override
	public OrderResponse getOrderById(Long id) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		return convertToResponse(order);
	}

	@Override
	public Page<OrderResponse> getAllOrders(Pageable pageable) {
		return orderRepository.findAll(pageable).map(this::convertToResponse);
	}

	@Override
	public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
		return orderRepository.findByStatus(status, pageable).map(this::convertToResponse);
	}

	@Override
	@Transactional
	public OrderResponse updateOrder(Long id, OrderRequest request) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (!order.isEditAllowed()) {
			throw new OrderNotEditableException(
					"Order cannot be edited. Edit window has expired or order is being processed.");
		}

		if (order.getStatus() != OrderStatus.PENDING) {
			throw new InvalidOrderStateException("Only pending orders can be edited");
		}

		// Update order items
		order.getOrderItems().clear();
		List<OrderItem> newItems = createOrderItems(request.getItems(), order);
		order.getOrderItems().addAll(newItems);

		// Recalculate total
		double total = newItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
		order.setTotalAmount(total);

		// Recalculate KPT
		calculateEstimatedKpt(order);

		order.setSpecialInstructions(request.getSpecialInstructions());

		order = orderRepository.save(order);
		return convertToResponse(order);
	}

	@Override
	@Transactional
	public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		OrderStatus currentStatus = order.getStatus();

		// Validate status transition
		validateStatusTransition(currentStatus, newStatus);

		order.setStatus(newStatus);

		// Update timestamps based on status
		switch (newStatus) {
		case CONFIRMED -> {
			order.setConfirmedAt(LocalDateTime.now());
			order.setIsEditable(false); // Lock editing
		}
		case IN_KITCHEN -> {
			order.setSentToKitchenAt(LocalDateTime.now());
			order.calculateEstimatedReadyTime();
		}
		case READY_TO_SERVE -> {
			order.setReadyAt(LocalDateTime.now());
			// Calculate actual KPT
			if (order.getSentToKitchenAt() != null) {
				long actualMinutes = ChronoUnit.MINUTES.between(order.getSentToKitchenAt(), order.getReadyAt());
				order.setActualKptMinutes((int) actualMinutes);
			}
		}
		case SERVED -> order.setServedAt(LocalDateTime.now());
		case COMPLETED -> {
			order.setCompletedAt(LocalDateTime.now());
			// Free up table
			if (order.getRestaurantTable() != null) {
				RestaurantTable table = order.getRestaurantTable();
				table.setStatus(TableStatus.AVAILABLE);
				tableRepository.save(table);
			}
		}
		case CANCELLED -> {
			// Free up table
			if (order.getRestaurantTable() != null) {
				RestaurantTable table = order.getRestaurantTable();
				table.setStatus(TableStatus.AVAILABLE);
				tableRepository.save(table);
			}
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + newStatus);
		}

		order = orderRepository.save(order);
		return convertToResponse(order);
	}

	@Override
	@Transactional
	public void cancelOrder(Long id) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getStatus() == OrderStatus.COMPLETED) {
			throw new InvalidOrderStateException("Cannot cancel completed order");
		}

		if (order.getStatus() == OrderStatus.SERVED) {
			throw new InvalidOrderStateException("Cannot cancel served order");
		}

		order.setStatus(OrderStatus.CANCELLED);

		if (order.getRestaurantTable() != null) {
			RestaurantTable table = order.getRestaurantTable();
			table.setStatus(TableStatus.AVAILABLE);
			tableRepository.save(table);
		}

		orderRepository.save(order);
	}

	@Override
	@Transactional
	public void sendWhatsAppBill(Long id) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getCustomerPhone() == null) {
			throw new IllegalArgumentException("Customer phone number is required");
		}

		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new InvalidOrderStateException("Can only send bill for completed orders");
		}

		whatsAppService.sendBill(order);
		order.setWhatsappBillSent(true);
		orderRepository.save(order);
	}

	// Helper methods

	private List<OrderItem> createOrderItems(List<OrderItemRequest> items, CustomerOrder order) {
		return items.stream().map(itemRequest -> {
			MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId()).orElseThrow(
					() -> new ResourceNotFoundException("Menu item not found: " + itemRequest.getMenuItemId()));

			if (!menuItem.getAvailable()) {
				throw new IllegalArgumentException("Menu item not available: " + menuItem.getName());
			}

			return OrderItem.builder().menuItem(menuItem).quantity(itemRequest.getQuantity()).price(menuItem.getPrice())
					.specialInstructions(itemRequest.getSpecialInstructions()).customerOrder(order).build();
		}).collect(Collectors.toList());
	}

	private void calculateEstimatedKpt(CustomerOrder order) {
		int maxKpt = 0;

		for (OrderItem item : order.getOrderItems()) {
			KitchenPreparationTime kpt = kptRepository.findByMenuItemId(item.getMenuItem().getId()).orElse(null);

			if (kpt != null) {
				maxKpt = Math.max(maxKpt, kpt.getEstimatedMinutes());
			} else {
				maxKpt = Math.max(maxKpt, 15); // Default 15 minutes
			}
		}

		order.setEstimatedKptMinutes(maxKpt);
	}

	private void validateStatusTransition(OrderStatus current, OrderStatus next) {
		List<OrderStatus> validTransitions = switch (current) {
		case PENDING -> Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
		case CONFIRMED -> Arrays.asList(OrderStatus.IN_KITCHEN, OrderStatus.CANCELLED);
		case IN_KITCHEN -> Arrays.asList(OrderStatus.READY_TO_SERVE, OrderStatus.CANCELLED);
		case READY_TO_SERVE -> Arrays.asList(OrderStatus.SERVED, OrderStatus.CANCELLED);
		case SERVED -> List.of(OrderStatus.COMPLETED);
		case COMPLETED, CANCELLED -> List.of(); // Terminal states
		};

		if (!validTransitions.contains(next)) {
			throw new InvalidOrderStateException("Invalid status transition from " + current + " to " + next);
		}
	}

	private OrderResponse convertToResponse(CustomerOrder order) {
		OrderResponse response = new OrderResponse();
		response.setId(order.getId());
		response.setOrderType(order.getOrderType());
		response.setStatus(order.getStatus());
		response.setTotalAmount(order.getTotalAmount());
		response.setCreatedAt(order.getCreatedAt());
		response.setEstimatedReadyTime(order.getEstimatedReadyTime());
		response.setEstimatedKptMinutes(order.getEstimatedKptMinutes());
		response.setIsEditable(order.isEditAllowed());
		response.setIsQrOrder(order.getIsQrOrder());
		response.setWhatsappBillSent(order.getWhatsappBillSent());

		if (order.getRestaurantTable() != null) {
			response.setTableName(order.getRestaurantTable().getName());
		}

		List<OrderResponse.OrderItemDto> itemDtos = order.getOrderItems().stream().map(item -> {
			OrderResponse.OrderItemDto dto = new OrderResponse.OrderItemDto();
			dto.setId(item.getId());
			dto.setMenuItemName(item.getMenuItem().getName());
			dto.setQuantity(item.getQuantity());
			dto.setPrice(item.getPrice());
			dto.setSubtotal(item.getSubtotal());
			dto.setSpecialInstructions(item.getSpecialInstructions());
			return dto;
		}).collect(Collectors.toList());

		response.setItems(itemDtos);
		return response;
	}
}
