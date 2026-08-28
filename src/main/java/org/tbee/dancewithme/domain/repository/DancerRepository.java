package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Dancer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DancerRepository extends CustomRepository<Dancer, Long> {

    Optional<Dancer> findByEmail(String email);

    Optional<Dancer> findByEmailConfirmationToken(String token);

    /**
     * Dancers that never confirmed their email and whose confirmation token has expired before the given moment.
     * A resend pushes the expiry forward, so such a dancer is really abandoned.
     */
    List<Dancer> findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(LocalDateTime moment);

    List<Dancer> findByActiveTrue();

    List<Dancer> findByActiveTrueAndPubliclyFindableTrue();
}
