package com.finly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDashboardResponse {
    private BigDecimal monthlyIncome;
    private BigDecimal currentFixedExpenses;
    private BigDecimal currentVariableExpenses;
    private BigDecimal currentSavings;

    private BigDecimal recommendedFixedExpenses;
    private BigDecimal recommendedVariableExpenses;
    private BigDecimal recommendedSavings;

    private BigDecimal fixedExpensesPercentage;
    private BigDecimal variableExpensesPercentage;
    private BigDecimal savingsPercentage;

    private BigDecimal recommendedFixedPercentage;
    private BigDecimal recommendedVariablePercentage;
    private BigDecimal recommendedSavingsPercentage;

    private BigDecimal availableBalance;
    private Boolean overBudget;
    private String recommendation;
}
