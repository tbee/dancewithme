package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerPhoto;

import java.util.List;

public interface DancerPhotoRepository extends CustomRepository<DancerPhoto, Long> {

    List<DancerPhoto> findByDancer(Dancer dancer);
}
