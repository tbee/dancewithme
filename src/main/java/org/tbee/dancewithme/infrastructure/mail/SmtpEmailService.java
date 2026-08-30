package org.tbee.dancewithme.infrastructure.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.tbee.dancewithme.application.EmailService;

/**
 * Sends email over SMTP using Spring's {@link JavaMailSender}.
 */
@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendConfirmationEmail(String to, String code, String confirmationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirm your 'Shall we Dance?' account");
        message.setText("Welcome to 'Shall we Dance?'\n\n"
                + "Your confirmation code is: " + code + "\n\n"
                + "Click the link below, or enter the code on the confirmation page:\n"
                + confirmationUrl + "\n\n"
                + "If you did not register, you can ignore this message.");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your 'Shall we Dance?' password");
        message.setText("We received a request to reset your 'Shall we Dance?' password.\n\n"
                + "Click the link below to choose a new password:\n"
                + resetUrl + "\n\n"
                + "If you did not request this, you can ignore this message.");
        mailSender.send(message);
    }
}
