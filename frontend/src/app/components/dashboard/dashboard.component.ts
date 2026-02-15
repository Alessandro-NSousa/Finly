import { Component, OnInit } from '@angular/core';
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
      },
      error: (error) => {
        console.error('Erro ao carregar dashboard', error);
        this.loading = false;
      }
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }
}
