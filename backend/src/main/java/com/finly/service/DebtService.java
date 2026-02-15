package com.finly.service;

import com.finly.dto.DebtRequest;
import com.finly.dto.DebtResponse;
import com.finly.dto.MonthlyReportResponse;
import com.finly.model.*;
import com.finly.repository.DebtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public DebtResponse createDebt(DebtRequest request) {
        User user = userService.getCurrentUserEntity();
        Debt debt = new Debt();
        debt.setName(request.getName());
        debt.setCategory(request.getCategory());
        debt.setAmount(request.getAmount());
        debt.setStatus(request.getStatus());
        debt.setReferenceMonth(request.getReferenceMonth());
        debt.setReferenceYear(request.getReferenceYear());
        debt.setType(request.getType());
        debt.setUser(user);
        debt.setIsFixedTemplate(request.getIsFixedTemplate() != null && request.getIsFixedTemplate());

        if (request.getType() == DebtType.PARCELADA && request.getTotalInstallments() != null) {
            debt = debtRepository.save(debt);
            createInstallments(debt, request.getTotalInstallments(), request.getAmount());
        } else {
            debt = debtRepository.save(debt);
        }

        if (debt.getIsFixedTemplate()) {
            replicateFixedDebtToRemainingMonths(debt);
        }

        return convertToResponse(debt);
    }

    @Transactional
    public void createInstallments(Debt parentDebt, Integer totalInstallments, BigDecimal totalAmount) {
        BigDecimal installmentAmount = totalAmount.divide(
                BigDecimal.valueOf(totalInstallments),
                2,
                RoundingMode.HALF_UP
        );

        int startMonth = parentDebt.getReferenceMonth();
        int startYear = parentDebt.getReferenceYear();

        for (int i = 1; i <= totalInstallments; i++) {
            Debt installment = new Debt();
            installment.setName(parentDebt.getName() + " - Parcela " + i + "/" + totalInstallments);
            installment.setCategory(parentDebt.getCategory());
            installment.setAmount(installmentAmount);
            installment.setStatus(DebtStatus.EM_ABERTO);
            installment.setType(DebtType.PARCELADA);
            installment.setUser(parentDebt.getUser());
            installment.setParentDebt(parentDebt);
            installment.setTotalInstallments(totalInstallments);
            installment.setCurrentInstallment(i);

            LocalDate installmentDate = LocalDate.of(startYear, startMonth, 1).plusMonths(i - 1);
            installment.setReferenceMonth(installmentDate.getMonthValue());
            installment.setReferenceYear(installmentDate.getYear());

            debtRepository.save(installment);
        }
    }

    @Transactional
    public void replicateFixedDebtToRemainingMonths(Debt fixedDebt) {
        LocalDate startDate = LocalDate.of(
                fixedDebt.getReferenceYear(),
                fixedDebt.getReferenceMonth(),
                1
        );

        // Replica para todos os meses até o final do ano
        LocalDate endOfYear = LocalDate.of(fixedDebt.getReferenceYear(), 12, 31);
        LocalDate currentMonth = startDate.plusMonths(1);

        while (currentMonth.isBefore(endOfYear.plusDays(1))) {
            List<Debt> existingDebts = debtRepository.findByUserIdAndReferenceMonthAndReferenceYear(
                    fixedDebt.getUser().getId(),
                    currentMonth.getMonthValue(),
                    currentMonth.getYear()
            );

            boolean alreadyExists = existingDebts.stream()
                    .anyMatch(d -> d.getName().equals(fixedDebt.getName()) && d.getIsFixedTemplate());

            if (!alreadyExists) {
                Debt replicatedDebt = new Debt();
                replicatedDebt.setName(fixedDebt.getName());
                replicatedDebt.setCategory(fixedDebt.getCategory());
                replicatedDebt.setAmount(fixedDebt.getAmount());
                replicatedDebt.setStatus(DebtStatus.EM_ABERTO);
                replicatedDebt.setReferenceMonth(currentMonth.getMonthValue());
                replicatedDebt.setReferenceYear(currentMonth.getYear());
                replicatedDebt.setType(DebtType.FIXA);
                replicatedDebt.setUser(fixedDebt.getUser());
                replicatedDebt.setIsFixedTemplate(true);

                debtRepository.save(replicatedDebt);
            }

            currentMonth = currentMonth.plusMonths(1);
        }
    }

    public List<DebtResponse> getDebtsByMonth(Integer month, Integer year) {
        User user = userService.getCurrentUserEntity();
        List<Debt> debts = debtRepository.findActiveDebtsByUserAndMonthYear(
                user.getId(), month, year
        );

        updateOverdueDebts(debts);

        return debts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DebtResponse updateDebtStatus(Long debtId, DebtStatus newStatus) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Dívida não encontrada"));

        User currentUser = userService.getCurrentUserEntity();
        if (!debt.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        debt.setStatus(newStatus);
        debt = debtRepository.save(debt);

        return convertToResponse(debt);
    }

    @Transactional
    public void deleteDebt(Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Dívida não encontrada"));

        User currentUser = userService.getCurrentUserEntity();
        if (!debt.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        debtRepository.delete(debt);
    }

    public MonthlyReportResponse getMonthlyReport(Integer month, Integer year) {
        User user = userService.getCurrentUserEntity();
        List<Debt> debts = debtRepository.findActiveDebtsByUserAndMonthYear(
                user.getId(), month, year
        );

        updateOverdueDebts(debts);

        BigDecimal totalDebts = debts.stream()
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = debts.stream()
                .filter(d -> d.getStatus() == DebtStatus.PAGA)
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOpen = debts.stream()
                .filter(d -> d.getStatus() == DebtStatus.EM_ABERTO || d.getStatus() == DebtStatus.ATRASADA)
                .map(Debt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal percentageCommitted = BigDecimal.ZERO;
        if (user.getMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
            percentageCommitted = totalDebts
                    .divide(user.getMonthlyIncome(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        MonthlyReportResponse report = new MonthlyReportResponse();
        report.setMonth(month);
        report.setYear(year);
        report.setTotalDebts(totalDebts);
        report.setTotalPaid(totalPaid);
        report.setTotalOpen(totalOpen);
        report.setMonthlyIncome(user.getMonthlyIncome());
        report.setPercentageCommitted(percentageCommitted);

        return report;
    }

    private void updateOverdueDebts(List<Debt> debts) {
        LocalDate today = LocalDate.now();
        for (Debt debt : debts) {
            if (debt.getStatus() == DebtStatus.EM_ABERTO) {
                LocalDate debtDate = LocalDate.of(debt.getReferenceYear(), debt.getReferenceMonth(), 1);
                if (debtDate.isBefore(LocalDate.of(today.getYear(), today.getMonthValue(), 1))) {
                    debt.setStatus(DebtStatus.ATRASADA);
                    debtRepository.save(debt);
                }
            }
        }
    }

    private DebtResponse convertToResponse(Debt debt) {
        DebtResponse response = new DebtResponse();
        response.setId(debt.getId());
        response.setName(debt.getName());
        response.setCategory(debt.getCategory());
        response.setAmount(debt.getAmount());
        response.setStatus(debt.getStatus());
        response.setReferenceMonth(debt.getReferenceMonth());
        response.setReferenceYear(debt.getReferenceYear());
        response.setType(debt.getType());
        response.setIsFixedTemplate(debt.getIsFixedTemplate());
        response.setTotalInstallments(debt.getTotalInstallments());
        response.setCurrentInstallment(debt.getCurrentInstallment());
        response.setParentDebtId(debt.getParentDebt() != null ? debt.getParentDebt().getId() : null);
        response.setCreatedAt(debt.getCreatedAt());
        response.setUpdatedAt(debt.getUpdatedAt());
        return response;
    }
}
