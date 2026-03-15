package com.finly.controller;

import com.finly.dto.AuthResponse;
import com.finly.dto.ForgotPasswordRequest;
import com.finly.dto.LoginRequest;
import com.finly.dto.RegisterRequest;
import com.finly.dto.RegisterResponse;
import com.finly.dto.ResetPasswordRequest;
import com.finly.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário e envia email de ativação")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate")
    @Operation(summary = "Ativar conta", description = "Ativa a conta do usuário através do token de verificação")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestParam String token) {
        String message = authService.activateAccount(token);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar email de verificação", description = "Reenvia o email de ativação para o usuário")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String message = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha", description = "Envia um email com link para redefinir a senha")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário através do token recebido por email")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
