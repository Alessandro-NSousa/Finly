package com.finly.controller;

import com.finly.dto.DebtRequest;
import com.finly.dto.DebtResponse;
import com.finly.dto.MonthlyReportResponse;
import com.finly.model.DebtStatus;
import com.finly.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debts")
@Tag(name = "Dívidas", description = "Gerenciamento de dívidas")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:4200")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @PostMapping
    @Operation(summary = "Criar nova dívida")
    public ResponseEntity<DebtResponse> createDebt(@Valid @RequestBody DebtRequest request) {
        DebtResponse debt = debtService.createDebt(request);
        return ResponseEntity.ok(debt);
    }

    @GetMapping
    @Operation(summary = "Listar dívidas por mês e ano")
    public ResponseEntity<List<DebtResponse>> getDebtsByMonth(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        List<DebtResponse> debts = debtService.getDebtsByMonth(month, year);
        return ResponseEntity.ok(debts);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status de uma dívida")
    public ResponseEntity<DebtResponse> updateDebtStatus(
            @PathVariable Long id,
            @RequestParam DebtStatus status
    ) {
        DebtResponse debt = debtService.updateDebtStatus(id, status);
        return ResponseEntity.ok(debt);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma dívida")
    public ResponseEntity<Void> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    @Operation(summary = "Obter relatório mensal")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        MonthlyReportResponse report = debtService.getMonthlyReport(month, year);
        return ResponseEntity.ok(report);
    }
}
