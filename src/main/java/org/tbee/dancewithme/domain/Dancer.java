package org.tbee.dancewithme.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Dancer extends BaseEntity<Dancer> {

    @Column(nullable = false, unique = true)
    private String email;

    // hashed password
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    private City city;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    private byte[] mugshot;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean publicProfile = true;

    @Column(columnDefinition = "text")
    private String whoami;

    @Column(columnDefinition = "text")
    private String whatdoiwant;

    // search preferences
    @Column(nullable = false)
    private int partnerAgeMin = 0;
    @Column(nullable = false)
    private int partnerAgeMax = 99;
    @Column(nullable = false)
    private int weekFrequencyMin = 0;
    @Column(nullable = false)
    private int weekFrequencyMax = 7;
    @Column(nullable = false)
    private int distanceToPartnerMax = 100;

    @OneToMany(mappedBy = "dancer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DancerDancestyle> dancestyles = new ArrayList<>();

    @OneToMany(mappedBy = "dancer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DancerPhoto> photos = new ArrayList<>();

    public String email() {
        return email;
    }
    public Dancer email(String email) {
        this.email = email;
        return this;
    }

    public String password() {
        return password;
    }
    public Dancer password(String password) {
        this.password = password;
        return this;
    }

    public String name() {
        return name;
    }
    public Dancer name(String name) {
        this.name = name;
        return this;
    }

    public LocalDate dateOfBirth() {
        return dateOfBirth;
    }
    public Dancer dateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public City city() {
        return city;
    }
    public Dancer city(City city) {
        this.city = city;
        return this;
    }

    public byte[] mugshot() {
        return mugshot;
    }
    public Dancer mugshot(byte[] mugshot) {
        this.mugshot = mugshot;
        return this;
    }

    public boolean active() {
        return active;
    }
    public Dancer active(boolean active) {
        this.active = active;
        return this;
    }

    public boolean publicProfile() {
        return publicProfile;
    }
    public Dancer publicProfile(boolean publicProfile) {
        this.publicProfile = publicProfile;
        return this;
    }

    public String whoami() {
        return whoami;
    }
    public Dancer whoami(String whoami) {
        this.whoami = whoami;
        return this;
    }

    public String whatdoiwant() {
        return whatdoiwant;
    }
    public Dancer whatdoiwant(String whatdoiwant) {
        this.whatdoiwant = whatdoiwant;
        return this;
    }

    public int partnerAgeMin() {
        return partnerAgeMin;
    }
    public Dancer partnerAgeMin(int partnerAgeMin) {
        this.partnerAgeMin = partnerAgeMin;
        return this;
    }

    public int partnerAgeMax() {
        return partnerAgeMax;
    }
    public Dancer partnerAgeMax(int partnerAgeMax) {
        this.partnerAgeMax = partnerAgeMax;
        return this;
    }

    public int weekFrequencyMin() {
        return weekFrequencyMin;
    }
    public Dancer weekFrequencyMin(int weekFrequencyMin) {
        this.weekFrequencyMin = weekFrequencyMin;
        return this;
    }

    public int weekFrequencyMax() {
        return weekFrequencyMax;
    }
    public Dancer weekFrequencyMax(int weekFrequencyMax) {
        this.weekFrequencyMax = weekFrequencyMax;
        return this;
    }

    public int distanceToPartnerMax() {
        return distanceToPartnerMax;
    }
    public Dancer distanceToPartnerMax(int distanceToPartnerMax) {
        this.distanceToPartnerMax = distanceToPartnerMax;
        return this;
    }

    public List<DancerDancestyle> dancestyles() {
        return Collections.unmodifiableList(dancestyles);
    }
    public Dancer dancestyles(List<DancerDancestyle> dancestyles) {
        this.dancestyles.clear();
        dancestyles.forEach(dancerDancestyle -> this.dancestyles.add(dancerDancestyle.dancer(this)));
        return this;
    }
    public DancerDancestyle addDancestyle(Dancestyle dancestyle, Role role) {
        DancerDancestyle dancerDancestyle = new DancerDancestyle()
                .dancer(this)
                .dancestyle(dancestyle)
                .role(role);
        dancestyles.add(dancerDancestyle);
        return dancerDancestyle;
    }

    public List<DancerPhoto> photos() {
        return Collections.unmodifiableList(photos);
    }
    public Dancer photos(List<DancerPhoto> photos) {
        this.photos.clear();
        photos.forEach(photo -> this.photos.add(photo.dancer(this)));
        return this;
    }
    public DancerPhoto addPhoto(byte[] image) {
        DancerPhoto photo = new DancerPhoto()
                .dancer(this)
                .image(image);
        photos.add(photo);
        return photo;
    }
}
