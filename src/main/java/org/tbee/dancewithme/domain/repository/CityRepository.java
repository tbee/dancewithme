package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.City;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends CustomRepository<City, Long> {

    Optional<City> findByGeonameId(Long geonameId);

    List<City> findAllByOrderByNameAsc();
}
