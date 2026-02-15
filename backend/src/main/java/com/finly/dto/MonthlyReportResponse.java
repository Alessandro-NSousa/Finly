package com.finly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    private Integer month;
    private Integer year;
    private BigDecimal totalDebts;
    private BigDecimal totalPaid;
    private BigDecimal totalOpen;
    private BigDecimal monthlyIncome;
    private BigDecimal percentageCommitted;
}
