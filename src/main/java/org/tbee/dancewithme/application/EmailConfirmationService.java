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
 * In development no email is sent, so the code can be prefilled in the UI to test the flow end to end.
 */
@Service
public class EmailConfirmationService {

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
        dancerRepository.save(dancer);
        if (!isDevelopment()) {
            emailService.sendConfirmationEmail(dancer.email(), code, confirmationUrl(code));
        }
        return code;
    }

    /**
     * Confirms the email for the dancer owning the given code.
     *
     * @return {@code true} when a dancer was confirmed, {@code false} when the code is unknown
     */
    @Transactional
    public boolean confirm(String code) {
        Dancer dancer = dancerRepository.findByEmailConfirmationToken(code).orElse(null);
        if (dancer == null) {
            return false;
        }
        dancer.emailConfirmedAt(LocalDateTime.now());
        dancer.emailConfirmationToken(null);
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
