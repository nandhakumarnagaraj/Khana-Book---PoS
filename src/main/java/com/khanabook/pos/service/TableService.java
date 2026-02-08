package com.khanabook.pos.service;

import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;

import java.util.List;

public interface TableService {

	RestaurantTable createTable(RestaurantTable table);

	RestaurantTable getTableById(Long id);

	List<RestaurantTable> getAllTables();

	List<RestaurantTable> getTablesByStatus(TableStatus status);

	RestaurantTable updateTableStatus(Long id, TableStatus status);

	String generateQrCode(Long id);
}
