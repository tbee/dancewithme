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

    public DancerService(DancerRepository dancerRepository, PasswordEncoder passwordEncoder) {
        this.dancerRepository = dancerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Dancer register(Dancer dancer, String rawPassword) {
        dancer.password(passwordEncoder.encode(rawPassword));
        dancer.privacyAgreementAcceptedAt(LocalDateTime.now());
        return dancerRepository.save(dancer);
    }

    @Transactional
    public Dancer update(Dancer dancer) {
        return dancerRepository.save(dancer);
    }

    @Transactional(readOnly = true)
    public List<DancerSearchingFor> searchingForOf(long dancerId) {
        Dancer dancer = dancerRepository.findById(dancerId).orElseThrow();
        // initialize the lazy relations needed by the views (open-in-view is disabled)
        dancer.searchingFor().forEach(entry -> {
            entry.dancestyle().name();
            entry.role().name();
        });
        return dancer.searchingFor();
    }

    @Transactional(readOnly = true)
    public Dancer loadWithDetails(long id) {
        Dancer dancer = dancerRepository.findById(id).orElseThrow();
        // initialize the lazy relations needed by the views (open-in-view is disabled)
        dancer.city();
        dancer.mugshot();
        dancer.dancestyles().forEach(dancerDancestyle -> {
            dancerDancestyle.dancestyle().name();
            dancerDancestyle.role().name();
        });
        dancer.photos().forEach(photo -> photo.image());
        return dancer;
    }
}
