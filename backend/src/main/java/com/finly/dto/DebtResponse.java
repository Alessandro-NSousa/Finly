package com.finly.dto;

import com.finly.model.DebtCategory;
import com.finly.model.DebtStatus;
import com.finly.model.DebtType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtResponse {
    private Long id;
    private String name;
    private DebtCategory category;
    private BigDecimal amount;
    private DebtStatus status;
    private Integer referenceMonth;
    private Integer referenceYear;
    private DebtType type;
    private Boolean isFixedTemplate;
    private Integer totalInstallments;
    private Integer currentInstallment;
    private Long parentDebtId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
