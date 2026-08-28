package org.tbee.dancewithme.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DancerService {

    private final DancerRepository dancerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfirmationService emailConfirmationService;

    public DancerService(DancerRepository dancerRepository, PasswordEncoder passwordEncoder,
                         EmailConfirmationService emailConfirmationService) {
        this.dancerRepository = dancerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailConfirmationService = emailConfirmationService;
    }

    /**
     * Registers a new dancer and starts the email confirmation flow.
     * When a dancer with the same email already exists but never confirmed that email, nothing is created:
     * the confirmation is simply resent for the existing profile. That profile is the one the confirmation
     * link belongs to, and after confirming, its owner can log in and correct the profile.
     *
     * @throws EmailAlreadyRegisteredException when a dancer with this email exists and has confirmed it
     */
    @Transactional
    public Registration registerOrResend(Dancer dancer, String rawPassword) {
        Dancer existing = dancerRepository.findByEmail(dancer.email()).orElse(null);
        if (existing != null) {
            if (existing.emailConfirmedAt() != null) {
                throw new EmailAlreadyRegisteredException(dancer.email());
            }
            emailConfirmationService.requestConfirmation(existing);
            return new Registration(existing, true);
        }

        dancer.password(passwordEncoder.encode(rawPassword));
        dancer.privacyAgreementAcceptedAt(LocalDateTime.now());
        Dancer saved = dancerRepository.save(dancer);
        emailConfirmationService.requestConfirmation(saved);
        return new Registration(saved, false);
    }

    /**
     * The outcome of a registration attempt.
     *
     * @param dancer the dancer the confirmation was sent for; the newly registered one, or the pre-existing
     *               unconfirmed one when {@code resentForExisting} is {@code true}
     * @param resentForExisting whether the registration was turned into a resend for an existing unconfirmed profile
     */
    public record Registration(Dancer dancer, boolean resentForExisting) {}

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("Email already registered: " + email);
        }
    }

    @Transactional
    public Dancer update(Dancer dancer) {
        return dancerRepository.save(dancer);
    }

    @Transactional
    public void delete(Dancer dancer) {
        dancerRepository.delete(dancer);
    }

    public List<DancerSearchingFor> searchingForOf(long dancerId) {
        Dancer dancer = dancerRepository.findById(dancerId).orElseThrow();
        return dancer.searchingFor();
    }

    public Dancer loadWithDetails(long id) {
        return dancerRepository.findById(id).orElseThrow();
    }
}
