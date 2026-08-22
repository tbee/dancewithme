package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class City extends BaseEntity<City> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Country country;

    @Column(nullable = false)
    private String name;

    // the id as assigned by geonames.org, from where cities can be imported
    @Column(unique = true)
    private Long geonameId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    public Country country() {
        return country;
    }
    public City country(Country country) {
        this.country = country;
        return this;
    }

    public String name() {
        return name;
    }
    public City name(String name) {
        this.name = name;
        return this;
    }

    public Long geonameId() {
        return geonameId;
    }
    public City geonameId(Long geonameId) {
        this.geonameId = geonameId;
        return this;
    }

    public double lat() {
        return lat;
    }
    public City lat(double lat) {
        this.lat = lat;
        return this;
    }

    public double lon() {
        return lon;
    }
    public City lon(double lon) {
        this.lon = lon;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + ", name=" + name;
    }
}
