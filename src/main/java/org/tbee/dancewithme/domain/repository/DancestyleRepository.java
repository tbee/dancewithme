package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Dancestyle;

import java.util.Optional;

public interface DancestyleRepository extends CustomRepository<Dancestyle, Long> {

    Optional<Dancestyle> findByName(String name);

    default Dancestyle findBallroom() {
        return findByName("Ballroom").orElseThrow();
    }
    default Dancestyle findLatin() {
        return findByName("Latin").orElseThrow();
    }
    default Dancestyle findWCS() {
        return findByName("West coast swing").orElseThrow();
    }
    default Dancestyle findSalsa() {
        return findByName("Salsa").orElseThrow();
    }
    default Dancestyle findTangoArgentine() {
        return findByName("Tango argentine").orElseThrow();
    }
}
