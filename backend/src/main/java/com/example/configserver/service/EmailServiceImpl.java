package com.example.configserver.service;

import com.example.configserver.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            
            String htmlMsg = "<p>Hello,</p>"
                    + "<p>You have requested to reset your password.</p>"
                    + "<p>Click the link below to change your password:</p>"
                    + "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
                    + "<p>This link will expire in 24 hours.</p>"
                    + "<p>If you did not request a password reset, please ignore this email or contact support.</p>"
                    + "<p>Regards,<br>Config Server Team</p>";
            
            helper.setText(htmlMsg, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String email, String username, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Welcome to Config Server");
            
            String htmlMsg = "<p>Hello " + username + ",</p>"
                    + "<p>Welcome to Config Server!</p>"
                    + "<p>Your account has been created successfully. To set up your password, please click the link below:</p>"
                    + "<p><a href=\"" + resetLink + "\">Set Password</a></p>"
                    + "<p>This link will expire in 24 hours.</p>"
                    + "<p>If you have any questions, please contact your administrator.</p>"
                    + "<p>Regards,<br>Config Server Team</p>";
            
            helper.setText(htmlMsg, true);
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage(), e);
        }
    }
} 