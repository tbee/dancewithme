package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.Sex;

@Entity
@Table(name = "dancer_dancestyle", uniqueConstraints = @UniqueConstraint(name = "dancer_dancestyle__dancer_dancestyle_UK", columnNames = {"dancer_id", "dancestyle_id"}))
public class DancerDancestyle extends BaseEntity<DancerDancestyle> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancer dancer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancestyle dancestyle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.EITHER;

    // 1 (absolute beginner) to 10 (world elite)
    @Column(nullable = false)
    private int skilllevel = 1;

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

    public int skilllevel() {
        return skilllevel;
    }
    public DancerDancestyle skilllevel(int skilllevel) {
        this.skilllevel = skilllevel;
        return this;
    }
}
