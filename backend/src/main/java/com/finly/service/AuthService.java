package com.finly.service;

import com.finly.dto.*;
import com.finly.model.PasswordResetToken;
import com.finly.model.User;
import com.finly.model.VerificationToken;
import com.finly.repository.PasswordResetTokenRepository;
import com.finly.repository.UserRepository;
import com.finly.repository.VerificationTokenRepository;
import com.finly.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        logger.info("Iniciando registro para email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Tentativa de registro com email já existente: {}", request.getEmail());
            throw new RuntimeException("Email já cadastrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMonthlyIncome(request.getMonthlyIncome());
        user.setEnabled(false);
        user.setEmailVerified(false);

        user = userRepository.save(user);
        logger.info("Usuário salvo no banco com ID: {}", user.getId());

        // Criar token de verificação
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
        
        verificationTokenRepository.save(verificationToken);
        logger.info("Token de verificação criado para usuário ID: {}", user.getId());

        // Enviar email de verificação
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
            logger.info("Email de verificação enviado para: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Erro ao enviar email de verificação para {}: {}", user.getEmail(), e.getMessage(), e);
            // Remove o usuário se o email falhar para manter consistência
            userRepository.delete(user);
            throw new RuntimeException(e.getMessage());
        }

        return new RegisterResponse(
            "Cadastro realizado com sucesso! Verifique seu email para ativar sua conta.",
            user.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (!user.getEnabled()) {
                throw new RuntimeException("Conta não ativada. Verifique seu email.");
            }

            return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
        } catch (DisabledException e) {
            throw new RuntimeException("Conta não ativada. Verifique seu email.");
        }
    }

    @Transactional
    public String activateAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (verificationToken.getUsed()) {
            throw new RuntimeException("Este link de ativação já foi utilizado");
        }

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Link de ativação expirado. Solicite um novo link.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        return "Conta ativada com sucesso! Você já pode fazer login.";
    }

    @Transactional
    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getEnabled()) {
            throw new RuntimeException("Esta conta já está ativada");
        }

        // Invalidar tokens antigos
        verificationTokenRepository.findByUser(user).ifPresent(oldToken -> {
            oldToken.setUsed(true);
            verificationTokenRepository.save(oldToken);
        });

        // Criar novo token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
        
        verificationTokenRepository.save(verificationToken);

        // Enviar email
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email. Tente novamente mais tarde.");
        }

        return "Email de verificação reenviado com sucesso!";
    }

    @Transactional
    public String forgotPassword(String email) {
        // Não revelamos se o email existe ou não por segurança
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.getEnabled()) {
                return;
            }
            // Invalida tokens anteriores
            passwordResetTokenRepository.deleteActiveTokensByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
                logger.info("Email de redefinição de senha enviado para: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Erro ao enviar email de reset para {}: {}", user.getEmail(), e.getMessage());
                throw new RuntimeException("Erro ao enviar email de redefinição. Tente novamente mais tarde.");
            }
        });
        return "Se esse email estiver cadastrado, você receberá um link para redefinir sua senha.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado."));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Este link já foi utilizado. Solicite um novo link.");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("Link expirado. Solicite um novo link de redefinição de senha.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Senha redefinida com sucesso para usuário ID: {}", user.getId());
        return "Senha redefinida com sucesso! Você já pode fazer login.";
    }
}
