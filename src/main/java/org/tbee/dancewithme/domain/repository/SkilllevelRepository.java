package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Skilllevel;

import java.util.List;

public interface SkilllevelRepository extends CustomRepository<Skilllevel, Long> {

    List<Skilllevel> findAllByOrderByLevelAsc();
}
