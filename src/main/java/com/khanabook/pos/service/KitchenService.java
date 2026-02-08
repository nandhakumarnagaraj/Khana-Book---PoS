package com.khanabook.pos.service;

import com.khanabook.pos.dto.response.KitchenOrderResponse;
import com.khanabook.pos.model.CustomerOrder;

import java.util.List;

public interface KitchenService {

	List<KitchenOrderResponse> getPendingOrders();

	List<KitchenOrderResponse> getActiveOrders();

	CustomerOrder markOrderReady(Long orderId);

	CustomerOrder updateKpt(Long orderId, Integer estimatedMinutes);
}
