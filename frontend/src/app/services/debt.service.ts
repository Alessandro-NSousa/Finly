import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Debt, DebtRequest, DebtStatus, MonthlyReport } from '../models/debt.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DebtService {
  private apiUrl = `${environment.apiUrl}/debts`;

  constructor(private http: HttpClient) {}

  createDebt(request: DebtRequest): Observable<Debt> {
    return this.http.post<Debt>(this.apiUrl, request);
  }

  getDebtsByMonth(month: number, year: number): Observable<Debt[]> {
    return this.http.get<Debt[]>(`${this.apiUrl}?month=${month}&year=${year}`);
  }

  updateDebtStatus(id: number, status: DebtStatus): Observable<Debt> {
    return this.http.put<Debt>(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  deleteDebt(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMonthlyReport(month: number, year: number): Observable<MonthlyReport> {
    return this.http.get<MonthlyReport>(`${this.apiUrl}/report?month=${month}&year=${year}`);
  }
}
