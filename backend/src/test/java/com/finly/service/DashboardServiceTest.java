package com.finly.service;

import com.finly.dto.FinancialDashboardResponse;
import com.finly.model.Debt;
import com.finly.model.DebtType;
import com.finly.model.User;
import com.finly.repository.DebtRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Locale BRAZILIAN_PORTUGUESE = Locale.forLanguageTag("pt-BR");

    @Mock
    private UserService userService;

    @Mock
    private DebtRepository debtRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getFinancialDashboardCalculatesRecommendedAmountsFromIncomeWithoutExceedingIt() {
        User user = buildUser(1L, "100.05");
        mockDashboardData(user, List.of());

        FinancialDashboardResponse dashboard = dashboardService.getFinancialDashboard();

        BigDecimal totalRecommended = dashboard.getRecommendedFixedExpenses()
                .add(dashboard.getRecommendedVariableExpenses())
                .add(dashboard.getRecommendedSavings());

        assertThat(dashboard.getRecommendedFixedExpenses()).isEqualByComparingTo("50.02");
        assertThat(dashboard.getRecommendedVariableExpenses()).isEqualByComparingTo("30.01");
        assertThat(dashboard.getRecommendedSavings()).isEqualByComparingTo("20.02");
        assertThat(totalRecommended).isEqualByComparingTo("100.05");
        assertThat(totalRecommended.compareTo(user.getMonthlyIncome())).isLessThanOrEqualTo(0);
        assertThat(dashboard.getRecommendation())
                .contains("50%")
                .contains("30%")
                .contains("20%")
                .contains(formatCurrency("50.02"))
                .contains(formatCurrency("30.01"))
                .contains(formatCurrency("20.02"));
    }

    @Test
    void getFinancialDashboardIncludesEquivalentValuesWhenBudgetNeedsAdjustment() {
        User user = buildUser(2L, "5000.00");
        mockDashboardData(user, List.of(
                buildDebt(user, DebtType.FIXA, "3000.00"),
                buildDebt(user, DebtType.VARIAVEL, "1800.00")
        ));

        FinancialDashboardResponse dashboard = dashboardService.getFinancialDashboard();

        assertThat(dashboard.getRecommendation())
                .contains("60%")
                .contains("36%")
                .contains("4%")
                .contains("50%")
                .contains("30%")
                .contains("20%")
                .contains(formatCurrency("2500.00"))
                .contains(formatCurrency("1500.00"))
                .contains(formatCurrency("1000.00"));
    }

    @Test
    void getFinancialDashboardKeepsTargetValuesInOverBudgetAlert() {
        User user = buildUser(3L, "5000.00");
        mockDashboardData(user, List.of(
                buildDebt(user, DebtType.FIXA, "4000.00"),
                buildDebt(user, DebtType.VARIAVEL, "1500.00")
        ));

        FinancialDashboardResponse dashboard = dashboardService.getFinancialDashboard();

        assertThat(dashboard.getOverBudget()).isTrue();
        assertThat(dashboard.getCurrentSavings()).isEqualByComparingTo("0");
        assertThat(dashboard.getRecommendation())
                .contains("ultrapassaram sua renda mensal")
                .contains("50%")
                .contains("30%")
                .contains("20%")
                .contains(formatCurrency("2500.00"))
                .contains(formatCurrency("1500.00"))
                .contains(formatCurrency("1000.00"));
    }

    private void mockDashboardData(User user, List<Debt> debts) {
        when(userService.getCurrentUserEntity()).thenReturn(user);
        when(debtRepository.findActiveDebtsByUserAndMonthYear(eq(user.getId()), anyInt(), anyInt()))
                .thenReturn(debts);
    }

    private User buildUser(Long id, String monthlyIncome) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário Teste");
        user.setEmail("dashboard" + id + "@finly.com");
        user.setPassword("senha");
        user.setMonthlyIncome(new BigDecimal(monthlyIncome));
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    private Debt buildDebt(User user, DebtType type, String amount) {
        Debt debt = new Debt();
        debt.setUser(user);
        debt.setType(type);
        debt.setAmount(new BigDecimal(amount));
        return debt;
    }

    private String formatCurrency(String amount) {
        return NumberFormat.getCurrencyInstance(BRAZILIAN_PORTUGUESE)
                .format(new BigDecimal(amount));
    }
}