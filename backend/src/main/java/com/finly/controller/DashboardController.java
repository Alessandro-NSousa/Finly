package com.finly.controller;

import com.finly.dto.FinancialDashboardResponse;
import com.finly.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard financeiro com recomendações")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Obter dashboard financeiro com recomendações")
    public ResponseEntity<FinancialDashboardResponse> getFinancialDashboard() {
        FinancialDashboardResponse dashboard = dashboardService.getFinancialDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
