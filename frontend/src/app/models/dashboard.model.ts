export interface FinancialDashboard {
  monthlyIncome: number;
  currentFixedExpenses: number;
  currentVariableExpenses: number;
  currentSavings: number;
  recommendedFixedExpenses: number;
  recommendedVariableExpenses: number;
  recommendedSavings: number;
  fixedExpensesPercentage: number;
  variableExpensesPercentage: number;
  savingsPercentage: number;
  recommendedFixedPercentage: number;
  recommendedVariablePercentage: number;
  recommendedSavingsPercentage: number;
  availableBalance: number;
  overBudget: boolean;
  recommendation: string;
}
