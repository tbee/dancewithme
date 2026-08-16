package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Dancer;

import java.util.Optional;

public interface DancerRepository extends CustomRepository<Dancer, Long> {

    Optional<Dancer> findByEmail(String email);
}
