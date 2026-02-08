package com.khanabook.pos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.OrderType;

public interface ReportService {

	List<CustomerOrder> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

	Double getTotalRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

	Map<String, Long> getOrderCountByStatus();

	Map<OrderType, Long> getOrderCountByType();
	// More complex reports can be added later
}
