package org.tbee.dancewithme.domain.repository;

import org.tbee.dancewithme.domain.Role;

import java.util.Optional;

public interface RoleRepository extends CustomRepository<Role, Long> {

    Optional<Role> findByName(String name);

    default Role findLead() {
        return findByName("lead").orElseThrow();
    }
    default Role findFollow() {
        return findByName("follow").orElseThrow();
    }
}
