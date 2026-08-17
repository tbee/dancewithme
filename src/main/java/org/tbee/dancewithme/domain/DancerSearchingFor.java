package org.tbee.dancewithme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A dancestyle + role the dancer is searching for in a partner.
 * This is deliberately separate from {@link DancerDancestyle} (what the dancer can do):
 * for example a 10-dance dancer can search for ballroom and latin partners separately.
 */
@Entity
@Table(name = "dancer_searching_for", uniqueConstraints = @UniqueConstraint(name = "dancer_searching_for__dancer_dancestyle_UK", columnNames = {"dancer_id", "dancestyle_id"}))
public class DancerSearchingFor extends BaseEntity<DancerSearchingFor> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancer dancer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancestyle dancestyle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Role role;

    public Dancer dancer() {
        return dancer;
    }
    public DancerSearchingFor dancer(Dancer dancer) {
        this.dancer = dancer;
        return this;
    }

    public Dancestyle dancestyle() {
        return dancestyle;
    }
    public DancerSearchingFor dancestyle(Dancestyle dancestyle) {
        this.dancestyle = dancestyle;
        return this;
    }

    public Role role() {
        return role;
    }
    public DancerSearchingFor role(Role role) {
        this.role = role;
        return this;
    }
}
