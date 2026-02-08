package com.khanabook.pos.service.impl;

import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.repository.RestaurantTableRepository;
import com.khanabook.pos.service.QrCodeService;
import com.khanabook.pos.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Override
    public String generateQrCodeForTable(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant Table not found with id: " + tableId));
        
        if (table.getQrToken() == null) {
            throw new IllegalStateException("QR token not generated for table: " + tableId);
        }

        try {
            return qrCodeGenerator.generateQrCode(table.getQrToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code for table " + tableId, e);
        }
    }
}
