package com.khanabook.pos.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    
    @NotNull(message = "Table ID is required")
    private Long tableId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number")
    private String customerPhone;
    
    @Email
    private String customerEmail;
    
    @NotNull @Min(value = 1, message = "Party size must be at least 1")
    @Max(value = 20, message = "Party size cannot exceed 20")
    private Integer partySize;
    
    @NotNull(message = "Booking date and time is required")
    @Future(message = "Booking must be in the future")
    private LocalDateTime bookingDateTime;
    
    private String specialRequests;
}
