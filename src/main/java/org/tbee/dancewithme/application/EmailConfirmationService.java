package org.tbee.dancewithme.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Orchestrates the email confirmation flow:
 * after registration a confirmation code is generated and emailed, and entering the code (or clicking the link)
 * marks the email as confirmed.
 * The code expires after {@value #TOKEN_EXPIRY_HOURS} hours; a resend generates a new code and restarts that clock.
 * Unconfirmed dancers whose code has expired are removed by {@link DancerCleanupService}, so the email address
 * becomes available again.
 * In development no email is sent, so the code can be prefilled in the UI to test the flow end to end.
 */
@Service
public class EmailConfirmationService {

    static final int TOKEN_EXPIRY_HOURS = 24;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final DancerRepository dancerRepository;
    private final EmailService emailService;
    private final Environment environment;
    private final String baseUrl;

    public EmailConfirmationService(DancerRepository dancerRepository, EmailService emailService,
                                    Environment environment, @Value("${baseUrl}") String baseUrl) {
        this.dancerRepository = dancerRepository;
        this.emailService = emailService;
        this.environment = environment;
        this.baseUrl = baseUrl;
    }

    /**
     * Generates a confirmation code for a newly registered dancer and emails it (except in development).
     *
     * @return the generated code, so it can be prefilled in the UI during development
     */
    @Transactional
    public String requestConfirmation(Dancer dancer) {
        String code = generateCode();
        dancer.emailConfirmationToken(code);
        dancer.emailConfirmationTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        dancerRepository.save(dancer);
        if (!isDevelopment()) {
            emailService.sendConfirmationEmail(dancer.email(), code, confirmationUrl(code));
        }
        return code;
    }

    /**
     * Generates a fresh confirmation code for the dancer owning the given email and emails it again
     * (except in development). The previous code stops working and the expiry clock restarts.
     * To avoid revealing whether an account exists, this returns silently when no dancer is found or
     * when the email is already confirmed.
     *
     * @return the generated code when a confirmation was resent, so it can be prefilled in the UI during
     *         development; {@code null} when there is nothing to confirm for this email
     */
    @Transactional
    public String resendConfirmation(String email) {
        Dancer dancer = dancerRepository.findByEmail(email).orElse(null);
        if (dancer == null || dancer.emailConfirmedAt() != null) {
            return null;
        }
        return requestConfirmation(dancer);
    }

    /**
     * Confirms the email for the dancer owning the given code.
     *
     * @return {@code true} when a dancer was confirmed, {@code false} when the code is unknown or expired
     */
    @Transactional
    public boolean confirm(String code) {
        Dancer dancer = dancerRepository.findByEmailConfirmationToken(code).orElse(null);
        if (dancer == null
                || dancer.emailConfirmationTokenExpiresAt() == null
                || dancer.emailConfirmationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        dancer.emailConfirmedAt(LocalDateTime.now());
        dancer.emailConfirmationToken(null);
        dancer.emailConfirmationTokenExpiresAt(null);
        dancerRepository.save(dancer);
        return true;
    }

    public boolean isDevelopment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private static String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String confirmationUrl(String code) {
        return baseUrl + "/confirm?code=" + code;
    }
}
