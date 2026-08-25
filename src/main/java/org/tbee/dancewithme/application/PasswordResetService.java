package org.tbee.dancewithme.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Orchestrates the password reset flow:
 * a dancer requests a reset via their email, a token is generated and emailed, and clicking the link
 * allows setting a new password. In development no email is sent, so the token can be prefilled in the UI.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final DancerRepository dancerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String baseUrl;

    public PasswordResetService(DancerRepository dancerRepository, EmailService emailService,
                                PasswordEncoder passwordEncoder, Environment environment,
                                @Value("${baseUrl}") String baseUrl) {
        this.dancerRepository = dancerRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.baseUrl = baseUrl;
    }

    /**
     * Generates a reset token for the dancer owning the given email and emails it (except in development).
     * To avoid revealing whether an account exists, this returns silently when no dancer is found.
     *
     * @return the generated token when an account exists, so it can be prefilled in the UI during development;
     *         {@code null} when no dancer is found for the email
     */
    @Transactional
    public String requestReset(String email) {
        Dancer dancer = dancerRepository.findByEmail(email).orElse(null);
        if (dancer == null) {
            return null;
        }
        String token = generateToken();
        dancer.passwordResetToken(token);
        dancer.passwordResetTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        dancerRepository.save(dancer);
        if (!isDevelopment()) {
            emailService.sendPasswordResetEmail(dancer.email(), resetUrl(dancer.email(), token));
        }
        return token;
    }

    /**
     * Sets a new password for the dancer owning the given email, if the token matches and is not expired.
     *
     * @return {@code true} when the password was reset, {@code false} when the email/token is unknown or expired
     */
    @Transactional
    public boolean resetPassword(String email, String token, String newPassword) {
        Dancer dancer = dancerRepository.findByEmail(email).orElse(null);
        if (dancer == null || dancer.passwordResetToken() == null
                || !dancer.passwordResetToken().equals(token)
                || dancer.passwordResetTokenExpiresAt() == null
                || dancer.passwordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        dancer.password(passwordEncoder.encode(newPassword));
        dancer.passwordResetToken(null);
        dancer.passwordResetTokenExpiresAt(null);
        dancerRepository.save(dancer);
        return true;
    }

    public boolean isDevelopment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resetUrl(String email, String token) {
        return baseUrl + "/reset-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&token=" + token;
    }
}
