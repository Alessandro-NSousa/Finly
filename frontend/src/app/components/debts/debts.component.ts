import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
      }
    });

    this.debtService.getMonthlyReport(this.currentMonth, this.currentYear).subscribe({
      next: (report) => {
        this.report = report;
      }
    });
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
      }
    });
  }

  updateStatus(debt: Debt, status: DebtStatus): void {
    this.debtService.updateDebtStatus(debt.id, status).subscribe({
      next: () => {
        this.loadDebts();
      }
    });
  }

  deleteDebt(debt: Debt): void {
    if (confirm(`Tem certeza que deseja excluir "${debt.name}"?`)) {
      this.debtService.deleteDebt(debt.id).subscribe({
        next: () => {
          this.loadDebts();
        }
      });
    }
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
}
