import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ChartData, ChartOptions } from 'chart.js';
import { DebtService } from '../../services/debt.service';
import { Debt, DebtCategory, DebtStatus, DebtType, MonthlyReport } from '../../models/debt.model';

@Component({
  selector: 'app-debts',
  templateUrl: './debts.component.html',
  styleUrls: ['./debts.component.css']
})
export class DebtsComponent implements OnInit {
  debts: Debt[] = [];
  report?: MonthlyReport;
  debtForm!: FormGroup;
  showForm = false;
  loading = false;

  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();

  categories = Object.values(DebtCategory);
  types = Object.values(DebtType);
  statuses = Object.values(DebtStatus);

  DebtStatus = DebtStatus;

  // Doughnut chart - debt status breakdown
  statusChartData: ChartData<'doughnut'> = {
    labels: ['Em Aberto', 'Paga', 'Atrasada'],
    datasets: [{
      data: [0, 0, 0],
      backgroundColor: ['#2196F3', '#4CAF50', '#f44336'],
      hoverBackgroundColor: ['#1976D2', '#388E3C', '#D32F2F'],
      borderWidth: 2
    }]
  };

  statusChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const value = ctx.raw as number;
            return ` ${new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)}`;
          }
        }
      }
    }
  };

  // Bar chart - expenses by category
  categoryChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{
      label: 'Total por Categoria (R$)',
      data: [],
      backgroundColor: [
        'rgba(255, 107, 107, 0.8)',
        'rgba(78, 205, 196, 0.8)',
        'rgba(149, 225, 211, 0.8)',
        'rgba(255, 193, 7, 0.8)',
        'rgba(156, 39, 176, 0.8)',
        'rgba(33, 150, 243, 0.8)',
        'rgba(158, 158, 158, 0.8)'
      ],
      borderWidth: 1
    }]
  };

  categoryChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    indexAxis: 'y' as const,
    scales: {
      x: {
        beginAtZero: true,
        ticks: {
          callback: (value) =>
            new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 }).format(value as number)
        }
      }
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const value = ctx.raw as number;
            return ` ${new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)}`;
          }
        }
      }
    }
  };

  constructor(
    private fb: FormBuilder,
    private debtService: DebtService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDebts();
  }

  initForm(): void {
    this.debtForm = this.fb.group({
      name: ['', Validators.required],
      category: [DebtCategory.OUTROS, Validators.required],
      amount: ['', [Validators.required, Validators.min(0)]],
      status: [DebtStatus.EM_ABERTO, Validators.required],
      referenceMonth: [this.currentMonth, Validators.required],
      referenceYear: [this.currentYear, Validators.required],
      type: [DebtType.VARIAVEL, Validators.required],
      isFixedTemplate: [false],
      totalInstallments: [null]
    });
  }

  loadDebts(): void {
    this.loading = true;
    this.debtService.getDebtsByMonth(this.currentMonth, this.currentYear).subscribe({
      next: (debts) => {
        this.debts = debts;
        this.loading = false;
        this.updateCharts(debts);
      },
      error: (error) => {
        this.loading = false;

        if (!this.isAuthError(error)) {
          console.error('Erro ao carregar dívidas', error);
        }
      }
    });

    this.debtService.getMonthlyReport(this.currentMonth, this.currentYear).subscribe({
      next: (report) => {
        this.report = report;
      },
      error: (error) => {
        if (!this.isAuthError(error)) {
          console.error('Erro ao carregar relatório mensal', error);
        }
      }
    });
  }

  private updateCharts(debts: Debt[]): void {
    // Status breakdown by amount
    const statusTotals: Record<string, number> = {
      [DebtStatus.EM_ABERTO]: 0,
      [DebtStatus.PAGA]: 0,
      [DebtStatus.ATRASADA]: 0
    };
    debts.forEach(d => { statusTotals[d.status] = (statusTotals[d.status] || 0) + d.amount; });

    this.statusChartData = {
      ...this.statusChartData,
      datasets: [{
        ...this.statusChartData.datasets[0],
        data: [
          statusTotals[DebtStatus.EM_ABERTO],
          statusTotals[DebtStatus.PAGA],
          statusTotals[DebtStatus.ATRASADA]
        ]
      }]
    };

    // Category breakdown by total amount
    const categoryTotals: Record<string, number> = {};
    debts.forEach(d => {
      const label = this.getCategoryLabel(d.category);
      categoryTotals[label] = (categoryTotals[label] || 0) + d.amount;
    });

    const sortedCategories = Object.entries(categoryTotals).sort((a, b) => b[1] - a[1]);

    this.categoryChartData = {
      ...this.categoryChartData,
      labels: sortedCategories.map(([label]) => label),
      datasets: [{
        ...this.categoryChartData.datasets[0],
        data: sortedCategories.map(([, value]) => value)
      }]
    };
  }

  onSubmit(): void {
    if (this.debtForm.invalid) {
      return;
    }

    const debtData = this.debtForm.value;
    this.debtService.createDebt(debtData).subscribe({
      next: () => {
        this.loadDebts();
        this.showForm = false;
        this.debtForm.reset();
        this.initForm();
      },
      error: (error) => {
        if (!this.isAuthError(error)) {
          console.error('Erro ao criar dívida', error);
        }
      }
    });
  }

  updateStatus(debt: Debt, status: DebtStatus): void {
    this.debtService.updateDebtStatus(debt.id, status).subscribe({
      next: () => {
        this.loadDebts();
      },
      error: (error) => {
        if (!this.isAuthError(error)) {
          console.error('Erro ao atualizar status da dívida', error);
        }
      }
    });
  }

  deleteDebt(debt: Debt): void {
    if (confirm(`Tem certeza que deseja excluir "${debt.name}"?`)) {
      this.debtService.deleteDebt(debt.id).subscribe({
        next: () => {
          this.loadDebts();
        },
        error: (error) => {
          if (!this.isAuthError(error)) {
            console.error('Erro ao excluir dívida', error);
          }
        }
      });
    }
  }

  private isAuthError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403);
  }

  changeMonth(delta: number): void {
    this.currentMonth += delta;
    if (this.currentMonth > 12) {
      this.currentMonth = 1;
      this.currentYear++;
    } else if (this.currentMonth < 1) {
      this.currentMonth = 12;
      this.currentYear--;
    }
    this.loadDebts();
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  getCategoryLabel(category: DebtCategory): string {
    const labels: Record<DebtCategory, string> = {
      [DebtCategory.MORADIA]: 'Moradia',
      [DebtCategory.TRANSPORTE]: 'Transporte',
      [DebtCategory.ALIMENTACAO]: 'Alimentação',
      [DebtCategory.LAZER]: 'Lazer',
      [DebtCategory.SAUDE]: 'Saúde',
      [DebtCategory.EDUCACAO]: 'Educação',
      [DebtCategory.OUTROS]: 'Outros'
    };
    return labels[category];
  }

  getStatusLabel(status: DebtStatus): string {
    const labels: Record<DebtStatus, string> = {
      [DebtStatus.EM_ABERTO]: 'Em Aberto',
      [DebtStatus.PAGA]: 'Paga',
      [DebtStatus.ATRASADA]: 'Atrasada'
    };
    return labels[status];
  }

  getTypeLabel(type: DebtType): string {
    const labels: Record<DebtType, string> = {
      [DebtType.FIXA]: 'Fixa',
      [DebtType.VARIAVEL]: 'Variável',
      [DebtType.PARCELADA]: 'Parcelada'
    };
    return labels[type];
  }

  hasChartData(): boolean {
    return this.debts.length > 0;
  }
}
