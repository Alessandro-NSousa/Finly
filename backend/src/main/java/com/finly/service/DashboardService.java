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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal RECOMMENDED_FIXED_PERCENTAGE = BigDecimal.valueOf(50);
    private static final BigDecimal RECOMMENDED_VARIABLE_PERCENTAGE = BigDecimal.valueOf(30);
    private static final BigDecimal RECOMMENDED_SAVINGS_PERCENTAGE = BigDecimal.valueOf(20);
    private static final Locale BRAZILIAN_PORTUGUESE = Locale.forLanguageTag("pt-BR");

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

        RecommendedAllocation recommendedAllocation = calculateRecommendedAllocation(user.getMonthlyIncome());

        BigDecimal fixedPercentage = calculatePercentage(fixedExpenses, user.getMonthlyIncome());
        BigDecimal variablePercentage = calculatePercentage(variableExpenses, user.getMonthlyIncome());
        BigDecimal savingsPercentage = calculatePercentage(currentSavings, user.getMonthlyIncome());

        BigDecimal availableBalance = user.getMonthlyIncome().subtract(totalExpenses);
        Boolean overBudget = totalExpenses.compareTo(user.getMonthlyIncome()) > 0;

        String recommendation = generateRecommendation(
        fixedPercentage, variablePercentage, savingsPercentage, overBudget, recommendedAllocation
        );

        FinancialDashboardResponse dashboard = new FinancialDashboardResponse();
        dashboard.setMonthlyIncome(user.getMonthlyIncome());
        dashboard.setCurrentFixedExpenses(fixedExpenses);
        dashboard.setCurrentVariableExpenses(variableExpenses);
        dashboard.setCurrentSavings(currentSavings);
    dashboard.setRecommendedFixedExpenses(recommendedAllocation.fixedExpenses());
    dashboard.setRecommendedVariableExpenses(recommendedAllocation.variableExpenses());
    dashboard.setRecommendedSavings(recommendedAllocation.savings());
        dashboard.setFixedExpensesPercentage(fixedPercentage);
        dashboard.setVariableExpensesPercentage(variablePercentage);
        dashboard.setSavingsPercentage(savingsPercentage);
    dashboard.setRecommendedFixedPercentage(RECOMMENDED_FIXED_PERCENTAGE);
    dashboard.setRecommendedVariablePercentage(RECOMMENDED_VARIABLE_PERCENTAGE);
    dashboard.setRecommendedSavingsPercentage(RECOMMENDED_SAVINGS_PERCENTAGE);
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
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private RecommendedAllocation calculateRecommendedAllocation(BigDecimal monthlyIncome) {
        BigDecimal safeIncome = monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : monthlyIncome;

        BigDecimal fixedExpenses = calculateRecommendedAmount(safeIncome, RECOMMENDED_FIXED_PERCENTAGE);
        BigDecimal variableExpenses = calculateRecommendedAmount(safeIncome, RECOMMENDED_VARIABLE_PERCENTAGE);
        BigDecimal savings = safeIncome.subtract(fixedExpenses)
                .subtract(variableExpenses)
                .setScale(2, RoundingMode.DOWN);

        if (savings.compareTo(BigDecimal.ZERO) < 0) {
            savings = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }

        return new RecommendedAllocation(fixedExpenses, variableExpenses, savings);
    }

    private BigDecimal calculateRecommendedAmount(BigDecimal income, BigDecimal percentage) {
        return income.multiply(percentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
    }

    private String generateRecommendation(BigDecimal fixedPct, BigDecimal variablePct,
                                         BigDecimal savingsPct, Boolean overBudget,
                                         RecommendedAllocation recommendedAllocation) {
        if (overBudget) {
            return "Atenção! Suas despesas ultrapassaram sua renda mensal. " +
                   "Revise seus gastos e tente reorganizar seu orçamento para até " +
                   formatTarget(RECOMMENDED_FIXED_PERCENTAGE, recommendedAllocation.fixedExpenses()) +
                   " em despesas fixas, até " +
                   formatTarget(RECOMMENDED_VARIABLE_PERCENTAGE, recommendedAllocation.variableExpenses()) +
                   " em despesas variáveis e " +
                   formatTarget(RECOMMENDED_SAVINGS_PERCENTAGE, recommendedAllocation.savings()) +
                   " em poupança.";
        }

        StringBuilder recommendation = new StringBuilder();

        if (fixedPct.compareTo(RECOMMENDED_FIXED_PERCENTAGE) > 0) {
            recommendation.append("Suas despesas fixas estão em ")
                    .append(formatPercentage(fixedPct))
                    .append(" da renda. O recomendado é até ")
                    .append(formatTarget(RECOMMENDED_FIXED_PERCENTAGE, recommendedAllocation.fixedExpenses()))
                    .append(". ");
        }

        if (variablePct.compareTo(RECOMMENDED_VARIABLE_PERCENTAGE) > 0) {
            recommendation.append("Suas despesas variáveis estão em ")
                    .append(formatPercentage(variablePct))
                    .append(" da renda. O recomendado é até ")
                    .append(formatTarget(RECOMMENDED_VARIABLE_PERCENTAGE, recommendedAllocation.variableExpenses()))
                    .append(". ");
        }

        if (savingsPct.compareTo(RECOMMENDED_SAVINGS_PERCENTAGE) < 0) {
            recommendation.append("Sua poupança está em ")
                    .append(formatPercentage(savingsPct))
                    .append(" da renda. Tente reservar pelo menos ")
                    .append(formatTarget(RECOMMENDED_SAVINGS_PERCENTAGE, recommendedAllocation.savings()))
                    .append(". ");
        }

        if (recommendation.length() == 0) {
            recommendation.append("Parabéns! Suas finanças estão equilibradas segundo a regra 50/30/20. ")
                    .append("Mantenha como referência até ")
                    .append(formatTarget(RECOMMENDED_FIXED_PERCENTAGE, recommendedAllocation.fixedExpenses()))
                    .append(" para despesas fixas, até ")
                    .append(formatTarget(RECOMMENDED_VARIABLE_PERCENTAGE, recommendedAllocation.variableExpenses()))
                    .append(" para despesas variáveis e ")
                    .append(formatTarget(RECOMMENDED_SAVINGS_PERCENTAGE, recommendedAllocation.savings()))
                    .append(" para poupança.");
        }

        return recommendation.toString().trim();
    }

    private String formatTarget(BigDecimal percentage, BigDecimal amount) {
        return formatPercentage(percentage) + " (" + formatCurrency(amount) + ")";
    }

    private String formatPercentage(BigDecimal percentage) {
        NumberFormat formatter = NumberFormat.getNumberInstance(BRAZILIAN_PORTUGUESE);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(percentage) + "%";
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(BRAZILIAN_PORTUGUESE).format(amount);
    }

    private record RecommendedAllocation(BigDecimal fixedExpenses,
                                         BigDecimal variableExpenses,
                                         BigDecimal savings) {
    }
}
