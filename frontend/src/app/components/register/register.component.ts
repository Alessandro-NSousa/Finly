import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  errorMessage = '';
  successMessage = '';
  loading = false;
  registeredEmail = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      monthlyIncome: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { name, email, password, monthlyIncome } = this.registerForm.value;
    this.authService.register(name, email, password, monthlyIncome).subscribe({
      next: (response) => {
        this.successMessage = response.message;
        this.registeredEmail = response.email;
        this.loading = false;
        this.registerForm.reset();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao criar conta. Tente novamente.';
        this.loading = false;
      }
    });
  }

  resendEmail(): void {
    if (!this.registeredEmail) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resendVerificationEmail(this.registeredEmail).subscribe({
      next: (response) => {
        this.successMessage = response.message;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao reenviar email.';
        this.loading = false;
      }
    });
  }
}
