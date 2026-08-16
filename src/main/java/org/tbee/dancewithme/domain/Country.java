package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Country extends BaseEntity<Country> {

    @Column(nullable = false, unique = true)
    private String name;

    public String name() {
        return name;
    }
    public Country name(String name) {
        this.name = name;
        return this;
    }
}
