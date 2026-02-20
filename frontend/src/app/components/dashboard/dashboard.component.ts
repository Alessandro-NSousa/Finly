import { Component, OnInit } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { DashboardService } from '../../services/dashboard.service';
import { UserService } from '../../services/user.service';
import { FinancialDashboard } from '../../models/dashboard.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  dashboard?: FinancialDashboard;
  user?: User;
  loading = true;

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
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
      }
    });

    this.dashboardService.getFinancialDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.loading = false;
        this.updateCharts(dashboard);
      },
      error: (error) => {
        console.error('Erro ao carregar dashboard', error);
        this.loading = false;
      }
    });
  }

  private updateCharts(dashboard: FinancialDashboard): void {
    this.expenseChartData = {
      ...this.expenseChartData,
      datasets: [{
        ...this.expenseChartData.datasets[0],
        data: [
          dashboard.currentFixedExpenses,
          dashboard.currentVariableExpenses,
          Math.max(0, dashboard.currentSavings)
        ]
      }]
    };

    this.comparisonChartData = {
      ...this.comparisonChartData,
      datasets: [
        {
          ...this.comparisonChartData.datasets[0],
          data: [
            parseFloat(dashboard.fixedExpensesPercentage.toFixed(1)),
            parseFloat(dashboard.variableExpensesPercentage.toFixed(1)),
            parseFloat(dashboard.savingsPercentage.toFixed(1))
          ]
        },
        this.comparisonChartData.datasets[1]
      ]
    };
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }
}
