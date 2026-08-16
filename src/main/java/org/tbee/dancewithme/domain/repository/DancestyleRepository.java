package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Dancestyle;

import java.util.Optional;

public interface DancestyleRepository extends CustomRepository<Dancestyle, Long> {

    Optional<Dancestyle> findByName(String name);
}
