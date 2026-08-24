package org.tbee.dancewithme.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Dancer extends BaseEntity<Dancer> implements SearchService.SearchParameters {

    @Column(nullable = false, unique = true)
    private String email;

    // hashed password
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex = Sex.UNKNOWN;

    @Column(nullable = false)
    private int yearOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    private City city;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    private byte[] mugshot;

    @Column(nullable = false)
    private boolean active = true;

    // whether the dancer can be found by anonymous (not logged in) users
    @Column(nullable = false)
    private boolean publiclyFindable = true;

    // when the dancer accepted the privacy agreement at registration
    private LocalDateTime privacyAgreementAcceptedAt;

    // when the dancer confirmed their email address (null = not yet confirmed)
    private LocalDateTime emailConfirmedAt;

    // the token used in the confirmation email; cleared once confirmed
    @Column(unique = true)
    private String emailConfirmationToken;

    @Column(columnDefinition = "text")
    private String whoami;

    @Column(columnDefinition = "text")
    private String whatdoiwant;

    // search preferences
    @Column(nullable = false)
    private int weekFrequencyMin = 0;
    @Column(nullable = false)
    private int weekFrequencyMax = 7;
    @Column(nullable = false)
    private int distanceMax = 100;
    @Column(nullable = false)
    private int ageDistanceMax = 100;

    @OneToMany(mappedBy = "dancer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DancerDancestyle> dancestyles = new ArrayList<>();

    // what the dancer is searching for in a partner (dancestyle + role)
    @OneToMany(mappedBy = "dancer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DancerSearchingFor> searchingFor = new ArrayList<>();

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

    public Sex sex() {
        return sex;
    }
    public Dancer sex(Sex sex) {
        this.sex = sex;
        return this;
    }

    public int yearOfBirth() {
        return yearOfBirth;
    }
    public Dancer yearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
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

    public boolean publiclyFindable() {
        return publiclyFindable;
    }
    public Dancer publiclyFindable(boolean publiclyFindable) {
        this.publiclyFindable = publiclyFindable;
        return this;
    }

    public LocalDateTime privacyAgreementAcceptedAt() {
        return privacyAgreementAcceptedAt;
    }
    public Dancer privacyAgreementAcceptedAt(LocalDateTime privacyAgreementAcceptedAt) {
        this.privacyAgreementAcceptedAt = privacyAgreementAcceptedAt;
        return this;
    }

    public LocalDateTime emailConfirmedAt() {
        return emailConfirmedAt;
    }
    public Dancer emailConfirmedAt(LocalDateTime emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
        return this;
    }

    public String emailConfirmationToken() {
        return emailConfirmationToken;
    }
    public Dancer emailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
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

    public int distanceMax() {
        return distanceMax;
    }
    public Dancer distanceMax(int distanceMax) {
        this.distanceMax = distanceMax;
        return this;
    }

    public int ageDistanceMax() {
        return ageDistanceMax;
    }
    public Dancer ageDistanceMax(int ageDistanceMax) {
        this.ageDistanceMax = ageDistanceMax;
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
    public DancerDancestyle addDancestyle(Dancestyle dancestyle, Role role, Skilllevel skilllevel) {
        DancerDancestyle dancerDancestyle = new DancerDancestyle()
                .dancer(this)
                .dancestyle(dancestyle)
                .role(role)
                .skilllevel(skilllevel);
        dancestyles.add(dancerDancestyle);
        return dancerDancestyle;
    }

    public List<DancerSearchingFor> searchingFor() {
        return Collections.unmodifiableList(searchingFor);
    }
    public Dancer searchingFor(List<DancerSearchingFor> searchingFor) {
        this.searchingFor.clear();
        searchingFor.forEach(entry -> this.searchingFor.add(entry.dancer(this)));
        return this;
    }
    public DancerSearchingFor addSearchingFor(Dancestyle dancestyle, SearchCriteriaSex sex, Role role, Skilllevel skilllevelMin, Skilllevel skilllevelMax) {
        DancerSearchingFor entry = new DancerSearchingFor()
                .dancer(this)
                .dancestyle(dancestyle)
                .sex(sex)
                .role(role)
                .skilllevelMin(skilllevelMin)
                .skilllevelMax(skilllevelMax);
        searchingFor.add(entry);
        return entry;
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

    @Override
    public String toString() {
        return super.toString() + ", email=" + email;
    }
}
