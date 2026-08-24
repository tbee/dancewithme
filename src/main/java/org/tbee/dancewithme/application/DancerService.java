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

    @Transactional
    public Dancer register(Dancer dancer, String rawPassword) {
        dancer.password(passwordEncoder.encode(rawPassword));
        dancer.privacyAgreementAcceptedAt(LocalDateTime.now());
        Dancer saved = dancerRepository.save(dancer);
        emailConfirmationService.requestConfirmation(saved);
        return saved;
    }

    @Transactional
    public Dancer update(Dancer dancer) {
        return dancerRepository.save(dancer);
    }

    public List<DancerSearchingFor> searchingForOf(long dancerId) {
        Dancer dancer = dancerRepository.findById(dancerId).orElseThrow();
        return dancer.searchingFor();
    }

    public Dancer loadWithDetails(long id) {
        return dancerRepository.findById(id).orElseThrow();
    }
}
