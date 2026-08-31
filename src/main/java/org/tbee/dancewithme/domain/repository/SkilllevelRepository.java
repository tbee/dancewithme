package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Skilllevel;

import java.util.List;
import java.util.Optional;

public interface SkilllevelRepository extends CustomRepository<Skilllevel, Long> {

    List<Skilllevel> findAllByOrderByLevelAsc();
    Optional<Skilllevel> findByCode(String code);
    Optional<Skilllevel> findByLevel(int level);
}
