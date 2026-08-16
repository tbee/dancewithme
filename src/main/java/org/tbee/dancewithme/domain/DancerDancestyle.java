package org.tbee.dancewithme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "dancer_dancestyle", uniqueConstraints = @UniqueConstraint(name = "dancer_dancestyle__dancer_dancestyle_UK", columnNames = {"dancer_id", "dancestyle_id"}))
public class DancerDancestyle extends BaseEntity<DancerDancestyle> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancer dancer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancestyle dancestyle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Role role;

    public Dancer dancer() {
        return dancer;
    }
    public DancerDancestyle dancer(Dancer dancer) {
        this.dancer = dancer;
        return this;
    }

    public Dancestyle dancestyle() {
        return dancestyle;
    }
    public DancerDancestyle dancestyle(Dancestyle dancestyle) {
        this.dancestyle = dancestyle;
        return this;
    }

    public Role role() {
        return role;
    }
    public DancerDancestyle role(Role role) {
        this.role = role;
        return this;
    }
}
