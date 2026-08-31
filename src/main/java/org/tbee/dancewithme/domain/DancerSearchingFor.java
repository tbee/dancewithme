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
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;

/**
 * A dancestyle + role the dancer is searching for in a partner.
 * This is deliberately separate from {@link DancerDancestyle} (what the dancer can do):
 * for example a 10-dance dancer can search for ballroom and latin partners separately.
 */
@Entity
@Table(name = "dancer_searching_for", uniqueConstraints = @UniqueConstraint(name = "dancer_searching_for__dancer_dancestyle_UK", columnNames = {"dancer_id", "dancestyle_id"}))
public class DancerSearchingFor extends BaseEntity<DancerSearchingFor> implements SearchService.SearchParametersStyles {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancer dancer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancestyle dancestyle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.EITHER;

    // the required sex of the partner
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchCriteriaSex sex = SearchCriteriaSex.EITHER;

    // the minimum and maximum skilllevel the searching dancer accepts in a partner
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Skilllevel skilllevelMin;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Skilllevel skilllevelMax;

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

    public SearchCriteriaSex sex() {
        return sex;
    }
    public DancerSearchingFor sex(SearchCriteriaSex sex) {
        this.sex = sex;
        return this;
    }

    public Skilllevel skilllevelMin() {
        return skilllevelMin;
    }
    public DancerSearchingFor skilllevelMin(Skilllevel skilllevelMin) {
        this.skilllevelMin = skilllevelMin;
        return this;
    }

    public Skilllevel skilllevelMax() {
        return skilllevelMax;
    }
    public DancerSearchingFor skilllevelMax(Skilllevel skilllevelMax) {
        this.skilllevelMax = skilllevelMax;
        return this;
    }
}
