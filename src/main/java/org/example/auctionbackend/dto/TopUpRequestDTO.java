package org.example.auctionbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpRequestDTO {
    @Min(value = 100, message = "Le montant doit être au moins 100 CHF")
    private Double amount;
}
