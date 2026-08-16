package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Country extends BaseEntity<Country> {

    @Column(nullable = false, unique = true)
    private String name;

    // ISO 3166-1 alpha-2 country code, lowercase, e.g. "nl"
    @Column(nullable = false, unique = true)
    private String iso;

    public String name() {
        return name;
    }
    public Country name(String name) {
        this.name = name;
        return this;
    }

    public String iso() {
        return iso;
    }
    public Country iso(String iso) {
        this.iso = iso;
        return this;
    }
}
