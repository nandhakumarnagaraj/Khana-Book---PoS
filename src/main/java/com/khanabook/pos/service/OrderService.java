package com.khanabook.pos.service;

import com.khanabook.pos.dto.request.OrderRequest;
import com.khanabook.pos.dto.request.QrOrderRequest;
import com.khanabook.pos.dto.response.OrderResponse;
import com.khanabook.pos.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

	OrderResponse createOrder(OrderRequest request);

	OrderResponse createQrOrder(QrOrderRequest request);

	OrderResponse getOrderById(Long id);

	Page<OrderResponse> getAllOrders(Pageable pageable);

	Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

	OrderResponse updateOrder(Long id, OrderRequest request);

	OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

	void cancelOrder(Long id);

	void sendWhatsAppBill(Long id);
}
