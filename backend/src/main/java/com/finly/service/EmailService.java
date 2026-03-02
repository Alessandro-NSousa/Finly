package com.finly.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.base.url:http://localhost:4200}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Finly - Ative sua conta";
        String verificationUrl = baseUrl + "/activate?token=" + token;
        
        String message = "Olá!\n\n" +
                "Obrigado por se cadastrar no Finly.\n\n" +
                "Para ativar sua conta, clique no link abaixo:\n" +
                verificationUrl + "\n\n" +
                "Este link expira em 24 horas.\n\n" +
                "Se você não se cadastrou no Finly, ignore este email.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Finly";

        sendEmail(to, subject, message);
    }

    public void sendEmail(String to, String subject, String text) {
        if (fromEmail == null || fromEmail.isEmpty()) {
            logger.error("Email não configurado. Configure MAIL_USERNAME nas variáveis de ambiente.");
            throw new RuntimeException("Servidor de email não configurado. Entre em contato com o administrador.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            logger.info("Enviando email para: {}", to);
            mailSender.send(message);
            logger.info("Email enviado com sucesso para: {}", to);
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o email de verificação. Verifique se o endereço de email está correto ou entre em contato com o suporte.");
        }
    }
}
