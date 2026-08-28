package org.tbee.dancewithme.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Removes dancers that registered but never confirmed their email address, once their confirmation token expired.
 * Without this an abandoned registration would keep its (unique) email address occupied forever, so nobody could
 * ever register under that address again.
 * Resending a confirmation pushes the expiry forward, so a dancer who is still busy confirming is never removed.
 */
@Service
public class DancerCleanupService {
    private static final Logger LOG = LoggerFactory.getLogger(DancerCleanupService.class);

    private final DancerRepository dancerRepository;

    public DancerCleanupService(DancerRepository dancerRepository) {
        this.dancerRepository = dancerRepository;
    }

    /**
     * Deletes all unconfirmed dancers whose confirmation token has expired.
     * Deletion goes through the entities, so the cascades on dancestyles, searchingFor and photos are applied.
     *
     * @return the number of deleted dancers
     */
    @Transactional
    public int purgeUnconfirmed() {
        List<Dancer> abandoned = dancerRepository.findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(LocalDateTime.now());
        if (abandoned.isEmpty()) {
            return 0;
        }
        LOG.info("Removing {} unconfirmed dancer(s) with an expired email confirmation", abandoned.size());
        dancerRepository.deleteAll(abandoned);
        return abandoned.size();
    }

    @Scheduled(cron = "0 7 * * * *")
    public void purgeUnconfirmedScheduled() {
        purgeUnconfirmed();
    }
}
