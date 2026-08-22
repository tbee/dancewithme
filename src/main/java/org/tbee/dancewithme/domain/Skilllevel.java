package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * How skilled a dancer is in a dancestyle.
 * The database only holds the code and the integer level (for comparing/searching);
 * the display name and description come from the translations (skilllevel.<code> and skilllevel.<code>.description).
 */
@Entity
public class Skilllevel extends BaseEntity<Skilllevel> {

    // for example: intermediate
    @Column(nullable = false, unique = true)
    private String code;

    // 1 (absolute beginner) to 10 (world elite), used to search and compare
    @Column(nullable = false, unique = true)
    private int level;

    public String code() {
        return code;
    }
    public Skilllevel code(String code) {
        this.code = code;
        return this;
    }

    public int level() {
        return level;
    }
    public Skilllevel level(int level) {
        this.level = level;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + ", code=" + code;
    }
}
