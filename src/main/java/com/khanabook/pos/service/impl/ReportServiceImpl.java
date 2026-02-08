package com.khanabook.pos.service.impl;

import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.model.OrderType;
import com.khanabook.pos.repository.CustomerOrderRepository;
import com.khanabook.pos.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CustomerOrderRepository customerOrderRepository;

    @Override
    public List<CustomerOrder> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return customerOrderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public Double getTotalRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return customerOrderRepository.findByCreatedAtBetween(startDate, endDate).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(CustomerOrder::getTotalAmount)
                .sum();
    }

    @Override
    public Map<String, Long> getOrderCountByStatus() {
        return customerOrderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getStatus().name(), Collectors.counting()));
    }

    @Override
    public Map<OrderType, Long> getOrderCountByType() {
        return customerOrderRepository.findAll().stream()
                .collect(Collectors.groupingBy(CustomerOrder::getOrderType, Collectors.counting()));
    }
}
