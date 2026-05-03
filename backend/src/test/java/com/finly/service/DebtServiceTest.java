package com.finly.service;

import com.finly.dto.DebtResponse;
import com.finly.dto.DebtUpdateRequest;
import com.finly.model.Debt;
import com.finly.model.DebtCategory;
import com.finly.model.DebtStatus;
import com.finly.model.DebtType;
import com.finly.model.User;
import com.finly.repository.DebtRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtServiceTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DebtService debtService;

    @Test
    void updateDebtUpdatesEditableFieldsForOwnedDebt() {
        User currentUser = buildUser(1L);
        Debt debt = buildDebt(10L, currentUser);
        DebtUpdateRequest request = new DebtUpdateRequest(
                "Plano de saúde",
                DebtCategory.SAUDE,
                new BigDecimal("420.75"),
                DebtStatus.PAGA
        );

        when(debtRepository.findById(10L)).thenReturn(Optional.of(debt));
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);
        when(debtRepository.save(any(Debt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DebtResponse response = debtService.updateDebt(10L, request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Plano de saúde");
        assertThat(response.getCategory()).isEqualTo(DebtCategory.SAUDE);
        assertThat(response.getAmount()).isEqualByComparingTo("420.75");
        assertThat(response.getStatus()).isEqualTo(DebtStatus.PAGA);
        assertThat(response.getType()).isEqualTo(DebtType.VARIAVEL);
        assertThat(response.getReferenceMonth()).isEqualTo(5);
        assertThat(response.getReferenceYear()).isEqualTo(2026);
        verify(debtRepository).save(debt);
    }

    @Test
    void updateDebtRejectsDebtOwnedByAnotherUser() {
        User currentUser = buildUser(1L);
        User debtOwner = buildUser(2L);
        Debt debt = buildDebt(20L, debtOwner);
        DebtUpdateRequest request = new DebtUpdateRequest(
                "Aluguel",
                DebtCategory.MORADIA,
                new BigDecimal("1800.00"),
                DebtStatus.EM_ABERTO
        );

        when(debtRepository.findById(20L)).thenReturn(Optional.of(debt));
        when(userService.getCurrentUserEntity()).thenReturn(currentUser);

        assertThatThrownBy(() -> debtService.updateDebt(20L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Acesso negado");

        verify(debtRepository, never()).save(any(Debt.class));
    }

    @Test
    void updateDebtThrowsWhenDebtDoesNotExist() {
        DebtUpdateRequest request = new DebtUpdateRequest(
                "Internet",
                DebtCategory.OUTROS,
                new BigDecimal("99.90"),
                DebtStatus.EM_ABERTO
        );

        when(debtRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> debtService.updateDebt(99L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Dívida não encontrada");

        verify(userService, never()).getCurrentUserEntity();
        verify(debtRepository, never()).save(any(Debt.class));
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário Teste");
        user.setEmail("teste" + id + "@finly.com");
        user.setPassword("senha");
        user.setMonthlyIncome(new BigDecimal("5000.00"));
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    private Debt buildDebt(Long id, User user) {
        Debt debt = new Debt();
        debt.setId(id);
        debt.setName("Conta de água");
        debt.setCategory(DebtCategory.MORADIA);
        debt.setAmount(new BigDecimal("150.00"));
        debt.setStatus(DebtStatus.EM_ABERTO);
        debt.setReferenceMonth(5);
        debt.setReferenceYear(2026);
        debt.setType(DebtType.VARIAVEL);
        debt.setIsFixedTemplate(false);
        debt.setUser(user);
        return debt;
    }
}