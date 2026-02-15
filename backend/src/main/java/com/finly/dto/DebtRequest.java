package com.finly.dto;

import com.finly.model.DebtCategory;
import com.finly.model.DebtStatus;
import com.finly.model.DebtType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtRequest {

    @NotBlank(message = "Nome da dívida é obrigatório")
    private String name;

    @NotNull(message = "Categoria é obrigatória")
    private DebtCategory category;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal amount;

    @NotNull(message = "Status é obrigatório")
    private DebtStatus status;

    @NotNull(message = "Mês de referência é obrigatório")
    private Integer referenceMonth;

    @NotNull(message = "Ano de referência é obrigatório")
    private Integer referenceYear;

    @NotNull(message = "Tipo é obrigatório")
    private DebtType type;

    private Boolean isFixedTemplate;

    private Integer totalInstallments;
}
