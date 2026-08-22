package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Dancestyle extends BaseEntity<Dancestyle> {

    // for example: ballroom, latin, 10-dans, social latin, wcs
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    public String name() {
        return name;
    }
    public Dancestyle name(String name) {
        this.name = name;
        return this;
    }

    public String description() {
        return description;
    }
    public Dancestyle description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + ", name=" + name;
    }
}
