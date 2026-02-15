export interface User {
  id: number;
  name: string;
  email: string;
  monthlyIncome: number;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  monthlyIncome: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  name: string;
  email: string;
}
