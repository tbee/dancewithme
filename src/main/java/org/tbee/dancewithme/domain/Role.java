package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Role extends BaseEntity<Role> {

    // for example: lead, follow
    @Column(nullable = false, unique = true)
    private String name;

    public String name() {
        return name;
    }
    public Role name(String name) {
        this.name = name;
        return this;
    }
}
