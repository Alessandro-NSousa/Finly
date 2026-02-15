export enum DebtCategory {
  MORADIA = 'MORADIA',
  TRANSPORTE = 'TRANSPORTE',
  ALIMENTACAO = 'ALIMENTACAO',
  LAZER = 'LAZER',
  SAUDE = 'SAUDE',
  EDUCACAO = 'EDUCACAO',
  OUTROS = 'OUTROS'
}

export enum DebtType {
  FIXA = 'FIXA',
  VARIAVEL = 'VARIAVEL',
  PARCELADA = 'PARCELADA'
}

export enum DebtStatus {
  EM_ABERTO = 'EM_ABERTO',
  PAGA = 'PAGA',
  ATRASADA = 'ATRASADA'
}

export interface Debt {
  id: number;
  name: string;
  category: DebtCategory;
  amount: number;
  status: DebtStatus;
  referenceMonth: number;
  referenceYear: number;
  type: DebtType;
  isFixedTemplate: boolean;
  totalInstallments?: number;
  currentInstallment?: number;
  parentDebtId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface DebtRequest {
  name: string;
  category: DebtCategory;
  amount: number;
  status: DebtStatus;
  referenceMonth: number;
  referenceYear: number;
  type: DebtType;
  isFixedTemplate?: boolean;
  totalInstallments?: number;
}

export interface MonthlyReport {
  month: number;
  year: number;
  totalDebts: number;
  totalPaid: number;
  totalOpen: number;
  monthlyIncome: number;
  percentageCommitted: number;
}
