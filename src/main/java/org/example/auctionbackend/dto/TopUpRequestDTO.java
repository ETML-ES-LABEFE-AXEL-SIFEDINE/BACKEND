package org.example.auctionbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpRequestDTO {
    @Min(value = 100, message = "The amount must be at least CHF 100")
    private Double amount;
}
