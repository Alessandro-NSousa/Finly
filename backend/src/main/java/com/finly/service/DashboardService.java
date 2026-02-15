package com.finly.service;

import com.finly.dto.FinancialDashboardResponse;
import com.finly.model.Debt;
import com.finly.model.DebtType;
import com.finly.model.User;
import com.finly.repository.DebtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private UserService userService;

    @Autowired
    private DebtRepository debtRepository;

    public FinancialDashboardResponse getFinancialDashboard() {
        User user = userService.getCurrentUserEntity();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        List<Debt> currentMonthDebts = debtRepository.findActiveDebtsByUserAndMonthYear(
                user.getId(), currentMonth, currentYear
        );

        BigDecimal fixedExpenses = currentMonthDebts.stream()
                .filter(d -> d.getType() == DebtType.FIXA || d.getType() == DebtType.PARCELADA)
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variableExpenses = currentMonthDebts.stream()
                .filter(d -> d.getType() == DebtType.VARIAVEL)
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = fixedExpenses.add(variableExpenses);
        BigDecimal currentSavings = user.getMonthlyIncome().subtract(totalExpenses);
        if (currentSavings.compareTo(BigDecimal.ZERO) < 0) {
            currentSavings = BigDecimal.ZERO;
        }

        BigDecimal recommendedFixed = user.getMonthlyIncome()
                .multiply(BigDecimal.valueOf(0.50));
        BigDecimal recommendedVariable = user.getMonthlyIncome()
                .multiply(BigDecimal.valueOf(0.30));
        BigDecimal recommendedSavings = user.getMonthlyIncome()
                .multiply(BigDecimal.valueOf(0.20));

        BigDecimal fixedPercentage = calculatePercentage(fixedExpenses, user.getMonthlyIncome());
        BigDecimal variablePercentage = calculatePercentage(variableExpenses, user.getMonthlyIncome());
        BigDecimal savingsPercentage = calculatePercentage(currentSavings, user.getMonthlyIncome());

        BigDecimal availableBalance = user.getMonthlyIncome().subtract(totalExpenses);
        Boolean overBudget = totalExpenses.compareTo(user.getMonthlyIncome()) > 0;

        String recommendation = generateRecommendation(
                fixedPercentage, variablePercentage, savingsPercentage, overBudget
        );

        FinancialDashboardResponse dashboard = new FinancialDashboardResponse();
        dashboard.setMonthlyIncome(user.getMonthlyIncome());
        dashboard.setCurrentFixedExpenses(fixedExpenses);
        dashboard.setCurrentVariableExpenses(variableExpenses);
        dashboard.setCurrentSavings(currentSavings);
        dashboard.setRecommendedFixedExpenses(recommendedFixed);
        dashboard.setRecommendedVariableExpenses(recommendedVariable);
        dashboard.setRecommendedSavings(recommendedSavings);
        dashboard.setFixedExpensesPercentage(fixedPercentage);
        dashboard.setVariableExpensesPercentage(variablePercentage);
        dashboard.setSavingsPercentage(savingsPercentage);
        dashboard.setRecommendedFixedPercentage(BigDecimal.valueOf(50));
        dashboard.setRecommendedVariablePercentage(BigDecimal.valueOf(30));
        dashboard.setRecommendedSavingsPercentage(BigDecimal.valueOf(20));
        dashboard.setAvailableBalance(availableBalance);
        dashboard.setOverBudget(overBudget);
        dashboard.setRecommendation(recommendation);

        return dashboard;
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateRecommendation(BigDecimal fixedPct, BigDecimal variablePct,
                                         BigDecimal savingsPct, Boolean overBudget) {
        if (overBudget) {
            return "Atenção! Suas despesas ultrapassaram sua renda mensal. " +
                   "Revise seus gastos e considere cortar despesas não essenciais.";
        }

        StringBuilder recommendation = new StringBuilder();

        if (fixedPct.compareTo(BigDecimal.valueOf(50)) > 0) {
            recommendation.append("Suas despesas fixas estão acima do recomendado (50%). ");
        }

        if (variablePct.compareTo(BigDecimal.valueOf(30)) > 0) {
            recommendation.append("Suas despesas variáveis estão acima do recomendado (30%). ");
        }

        if (savingsPct.compareTo(BigDecimal.valueOf(20)) < 0) {
            recommendation.append("Tente aumentar sua poupança para pelo menos 20% da renda. ");
        }

        if (recommendation.length() == 0) {
            recommendation.append("Parabéns! Suas finanças estão equilibradas segundo a regra 50/30/20.");
        }

        return recommendation.toString().trim();
    }
}
