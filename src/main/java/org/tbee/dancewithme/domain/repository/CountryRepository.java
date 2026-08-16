package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Country;

import java.util.Optional;

public interface CountryRepository extends CustomRepository<Country, Long> {

    Optional<Country> findByName(String name);
}
