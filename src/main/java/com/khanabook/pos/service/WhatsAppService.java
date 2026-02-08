package com.khanabook.pos.service;

import com.khanabook.pos.model.CustomerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WhatsAppService {

        @Value("${whatsapp.api.url:}")
        private String apiUrl;

        @Value("${whatsapp.api.key:}")
        private String apiKey;

        public void sendBill(CustomerOrder order) {
                // Implement WhatsApp Business API integration
                // This is a placeholder implementation

                String message = buildBillMessage(order);
                log.info("Sending WhatsApp bill to {}: {}", order.getCustomerPhone(), message);

                // TODO: Integrate with actual WhatsApp Business API
                // Example: Use Twilio, MessageBird, or WhatsApp Cloud API
        }

        private String buildBillMessage(CustomerOrder order) {
                StringBuilder message = new StringBuilder();
                message.append("Thank you for dining with Khana Book!");
                message.append("Order ID: ").append(order.getId()).append("");
                message.append("Table: ").append(
                                order.getRestaurantTable() != null ? order.getRestaurantTable().getName() : "Takeaway")
                                .append("");

                message.append("Items:");
                order.getOrderItems().forEach(item -> {
                        message.append("- ").append(item.getMenuItem().getName())
                                        .append(" x").append(item.getQuantity())
                                        .append(" = ₹").append(item.getSubtotal())
                                        .append("");
                });

                message.append("Total: ₹").append(order.getTotalAmount());
                message.append("Visit again!");

                return message.toString();
        }
}
