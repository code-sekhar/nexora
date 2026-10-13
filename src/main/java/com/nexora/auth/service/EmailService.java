package com.nexora.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetOtp(
            String toEmail,
            String otp
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("support@eastalya.com");
        message.setTo(toEmail);
        message.setSubject("Nexora Password Reset OTP");

        message.setText(
                "Hello,\n\n" +
                "Your Nexora password reset OTP is:\n\n" +
                otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Regards,\n" +
                "Nexora Team"
        );

        mailSender.send(message);
    }
}