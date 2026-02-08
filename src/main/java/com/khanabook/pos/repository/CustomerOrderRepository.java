package com.khanabook.pos.repository;

import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.model.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
       Page<CustomerOrder> findByStatus(OrderStatus status, Pageable pageable);

       Page<CustomerOrder> findByOrderType(OrderType orderType, Pageable pageable);

       List<CustomerOrder> findByStatusIn(List<OrderStatus> statuses);

       @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.orderItems oi " +
                     "LEFT JOIN FETCH oi.menuItem WHERE o.status IN :statuses")
       List<CustomerOrder> findByStatusInWithItems(@Param("statuses") List<OrderStatus> statuses);

       @Query("SELECT o.status, COUNT(o) FROM CustomerOrder o GROUP BY o.status")
       List<Object[]> countByStatus();

       @Query("SELECT o.orderType, COUNT(o) FROM CustomerOrder o GROUP BY o.orderType")
       List<Object[]> countByType();

       List<CustomerOrder> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

       @Query("SELECT o FROM com.khanabook.pos.model.CustomerOrder o WHERE o.restaurantTable.id = :tableId " +
                     "AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
       List<CustomerOrder> findActiveOrdersByTableId(@Param("tableId") Long tableId);
}
