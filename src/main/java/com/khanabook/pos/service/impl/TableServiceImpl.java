package com.khanabook.pos.service.impl;

import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;
import com.khanabook.pos.repository.RestaurantTableRepository;
import com.khanabook.pos.service.TableService;
import com.khanabook.pos.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final RestaurantTableRepository tableRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Override @Transactional @CacheEvict(value = "tables", allEntries = true)
    public RestaurantTable createTable(RestaurantTable table) {
        if (tableRepository.existsByName(table.getName())) {
            throw new IllegalArgumentException("Table name already exists");
        }

        table.setStatus(TableStatus.AVAILABLE);
        table = tableRepository.save(table);

        // Generate QR code
        try {
            String qrCode = qrCodeGenerator.generateQrCode(table.getQrToken());
            table.setQrCode(qrCode);
            table = tableRepository.save(table);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }

        return table;
    }

    @Override
    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    @Override @Cacheable("tables")
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Override
    public List<RestaurantTable> getTablesByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    @Override @Transactional @CacheEvict(value = "tables", allEntries = true)
    public RestaurantTable updateTableStatus(Long id, TableStatus status) {
        RestaurantTable table = getTableById(id);
        table.setStatus(status);
        return tableRepository.save(table);
    }

    @Override
    public String generateQrCode(Long id) {
        RestaurantTable table = getTableById(id);
        return table.getQrCode();
    }
}
