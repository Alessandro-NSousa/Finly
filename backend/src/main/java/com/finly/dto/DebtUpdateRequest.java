package com.finly.dto;

import com.finly.model.DebtCategory;
import com.finly.model.DebtStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtUpdateRequest {

    @NotBlank(message = "Nome da dívida é obrigatório")
    private String name;

    @NotNull(message = "Categoria é obrigatória")
    private DebtCategory category;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal amount;

    @NotNull(message = "Status é obrigatório")
    private DebtStatus status;
}