import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ChartData, ChartOptions } from 'chart.js';
import { Subscription } from 'rxjs';
import { DashboardService } from '../../services/dashboard.service';
import { ThemeService } from '../../services/theme.service';
import { UserService } from '../../services/user.service';
import { FinancialDashboard } from '../../models/dashboard.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  dashboard?: FinancialDashboard;
  user?: User;
  loading = true;
  private themeSubscription?: Subscription;

  // Doughnut chart - expense breakdown
  expenseChartData: ChartData<'doughnut'> = {
    labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
    datasets: [{
      data: [0, 0, 0],
      backgroundColor: ['#FF6B6B', '#4ECDC4', '#95E1D3'],
      hoverBackgroundColor: ['#FF5252', '#3DBDB5', '#7FD6C5'],
      borderWidth: 2
    }]
  };

  expenseChartOptions: ChartOptions<'doughnut'> = {
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

  // Bar chart - 50/30/20 comparison
  comparisonChartData: ChartData<'bar'> = {
    labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
    datasets: [
      {
        label: 'Atual (%)',
        data: [0, 0, 0],
        backgroundColor: 'rgba(255, 107, 107, 0.8)',
        borderColor: '#FF6B6B',
        borderWidth: 1
      },
      {
        label: 'Ideal (%)',
        data: [50, 30, 20],
        backgroundColor: 'rgba(78, 205, 196, 0.8)',
        borderColor: '#4ECDC4',
        borderWidth: 1
      }
    ]
  };

  comparisonChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        ticks: { callback: (value) => `${value}%` }
      }
    },
    plugins: {
      legend: { position: 'bottom' },
      tooltip: {
        callbacks: {
          label: (ctx) => ` ${ctx.dataset.label}: ${(ctx.raw as number).toFixed(1)}%`
        }
      }
    }
  };

  constructor(
    private dashboardService: DashboardService,
    private userService: UserService,
    private themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.themeSubscription = this.themeService.theme$.subscribe(() => {
      this.applyChartTheme();
    });

    this.loadData();
  }

  ngOnDestroy(): void {
    this.themeSubscription?.unsubscribe();
  }

  loadData(): void {
    this.loading = true;

    this.userService.getCurrentUser().subscribe({
      next: (user) => { this.user = user; },
      error: (error) => {
        if (!this.isAuthError(error)) {
          console.error('Erro ao carregar dados do usuário', error);
        }
      }
    });

    this.dashboardService.getFinancialDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.loading = false;
        this.updateCharts(dashboard);
      },
      error: (error) => {
        if (!this.isAuthError(error)) {
          console.error('Erro ao carregar dashboard', error);
        }
        this.loading = false;
      }
    });
  }

  private isAuthError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403);
  }

  private updateCharts(dashboard: FinancialDashboard): void {
    const currentExpenseData = [
      dashboard.currentFixedExpenses,
      dashboard.currentVariableExpenses,
      Math.max(0, dashboard.currentSavings)
    ];

    const currentComparisonData = [
      parseFloat(dashboard.fixedExpensesPercentage.toFixed(1)),
      parseFloat(dashboard.variableExpensesPercentage.toFixed(1)),
      parseFloat(dashboard.savingsPercentage.toFixed(1))
    ];

    this.expenseChartData = {
      labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
      datasets: [this.buildExpenseDataset(currentExpenseData)]
    };

    this.comparisonChartData = {
      labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
      datasets: this.buildComparisonDatasets(currentComparisonData)
    };

    this.applyChartTheme();
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }

  private applyChartTheme(): void {
    const expenseValues = (this.expenseChartData.datasets[0]?.data as number[] | undefined) ?? [0, 0, 0];
    const currentValues = (this.comparisonChartData.datasets[0]?.data as number[] | undefined) ?? [0, 0, 0];

    this.expenseChartData = {
      labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
      datasets: [this.buildExpenseDataset(expenseValues)]
    };

    this.comparisonChartData = {
      labels: ['Despesas Fixas', 'Despesas Variáveis', 'Poupança'],
      datasets: this.buildComparisonDatasets(currentValues)
    };

    this.expenseChartOptions = this.buildExpenseChartOptions();
    this.comparisonChartOptions = this.buildComparisonChartOptions();
  }

  private buildExpenseDataset(values: number[]) {
    return {
      data: values,
      backgroundColor: [
        this.cssVar('--chart-expense-1'),
        this.cssVar('--chart-expense-2'),
        this.cssVar('--chart-expense-3')
      ],
      hoverBackgroundColor: [
        this.cssVar('--chart-expense-1'),
        this.cssVar('--chart-expense-2'),
        this.cssVar('--chart-expense-3')
      ],
      borderColor: this.cssVar('--chart-surface'),
      borderWidth: 4
    };
  }

  private buildComparisonDatasets(values: number[]) {
    return [
      {
        label: 'Atual (%)',
        data: values,
        backgroundColor: this.cssVar('--chart-bar-1'),
        borderColor: this.cssVar('--chart-expense-1'),
        borderWidth: 1
      },
      {
        label: 'Ideal (%)',
        data: [50, 30, 20],
        backgroundColor: this.cssVar('--chart-bar-3'),
        borderColor: this.cssVar('--chart-expense-3'),
        borderWidth: 1
      }
    ];
  }

  private buildExpenseChartOptions(): ChartOptions<'doughnut'> {
    return {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '66%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: this.cssVar('--chart-axis'),
            usePointStyle: true,
            padding: 18,
            boxWidth: 10
          }
        },
        tooltip: {
          backgroundColor: this.cssVar('--chart-tooltip-bg'),
          titleColor: this.cssVar('--chart-tooltip-text'),
          bodyColor: this.cssVar('--chart-tooltip-text'),
          borderColor: this.cssVar('--border-strong'),
          borderWidth: 1,
          callbacks: {
            label: (ctx) => {
              const value = ctx.raw as number;
              return ` ${new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)}`;
            }
          }
        }
      }
    };
  }

  private buildComparisonChartOptions(): ChartOptions<'bar'> {
    return {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          ticks: {
            color: this.cssVar('--chart-axis')
          },
          grid: {
            display: false
          }
        },
        y: {
          beginAtZero: true,
          max: 100,
          ticks: {
            color: this.cssVar('--chart-axis'),
            callback: (value) => `${value}%`
          },
          grid: {
            color: this.cssVar('--chart-grid')
          }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: this.cssVar('--chart-axis'),
            usePointStyle: true,
            padding: 18,
            boxWidth: 10
          }
        },
        tooltip: {
          backgroundColor: this.cssVar('--chart-tooltip-bg'),
          titleColor: this.cssVar('--chart-tooltip-text'),
          bodyColor: this.cssVar('--chart-tooltip-text'),
          borderColor: this.cssVar('--border-strong'),
          borderWidth: 1,
          callbacks: {
            label: (ctx) => ` ${ctx.dataset.label}: ${(ctx.raw as number).toFixed(1)}%`
          }
        }
      }
    };
  }

  private cssVar(name: string): string {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  }
}
