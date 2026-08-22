package org.tbee.dancewithme.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DancerRepository dancerRepository;
    @Mock
    private SecurityService securityService;

    private SearchService searchService;

    private final Dancestyle ballroom = dancestyle(1L, "Ballroom");
    private final Dancestyle latin = dancestyle(2L, "Latin");
    private final Role lead = role(1L, "lead");
    private final Role follow = role(2L, "follow");
    private final Skilllevel beginner = skilllevel(1L, "absolute_beginner", 1);
    private final Skilllevel novice = skilllevel(2L, "novice", 2);
    private final Skilllevel intermediate = skilllevel(3L, "intermediate_social", 3);
    private final City amsterdam = city(1L, "Amsterdam", 52.3676, 4.9041);
    private final City rotterdam = city(2L, "Rotterdam", 51.9225, 4.47917);

    @BeforeEach
    void setUp() {
        searchService = new SearchService(dancerRepository, securityService);
    }

    // ------------------------- candidate sourcing -------------------------

    @Test
    void searchAnonymousQueriesOnlyPubliclyFindableCandidates() {
        stubAnonymous();

        when(dancerRepository.findByActiveTrueAndPubliclyFindableTrue()).thenReturn(List.of());

        searchService.search(params(List.of(), 100, 0, 7, 100));

        verify(dancerRepository).findByActiveTrueAndPubliclyFindableTrue();
        verify(dancerRepository, never()).findByActiveTrue();
    }

    @Test
    void searchLoggedInQueriesAllActiveCandidates() {
        Dancer current = dancer(1L, "Me", Sex.MALE, 1990, amsterdam);
        stubLoggedIn(current);

        when(dancerRepository.findByActiveTrue()).thenReturn(List.of());

        searchService.search(params(List.of(), 100, 0, 7, 100));

        verify(dancerRepository).findByActiveTrue();
        verify(dancerRepository, never()).findByActiveTrueAndPubliclyFindableTrue();
    }

    // ------------------------- dancestyle matching -------------------------

    @Test
    void matchesCandidateOnDancestyle() {
        stubAnonymous();

        Dancer ballroomDancer = candidate(2L, "BallroomDancer", Sex.FEMALE, 1990, amsterdam);
        ballroomDancer.addDancestyle(ballroom, follow, intermediate);
        Dancer latinDancer = candidate(3L, "LatinDancer", Sex.FEMALE, 1990, amsterdam);
        latinDancer.addDancestyle(latin, follow, intermediate);

        SearchService.SearchParameters criteria = params(
                List.of(style(ballroom, null, SearchCriteriaSex.EITHER, null, null)),
                100, 0, 7, 100);

        List<SearchService.SearchResult> results = searchService.match(criteria, List.of(ballroomDancer, latinDancer));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("BallroomDancer");
    }

    @Test
    void matchesAnyOfTheRequestedDancestyles() {
        stubAnonymous();

        Dancer latinDancer = candidate(2L, "LatinDancer", Sex.FEMALE, 1990, amsterdam);
        latinDancer.addDancestyle(latin, follow, intermediate);

        SearchService.SearchParameters criteria = params(
                List.of(
                        style(ballroom, null, SearchCriteriaSex.EITHER, null, null),
                        style(latin, null, SearchCriteriaSex.EITHER, null, null)),
                100, 0, 7, 100);

        List<SearchService.SearchResult> results = searchService.match(criteria, List.of(latinDancer));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("LatinDancer");
    }

    @Test
    void emptySearchingForMatchesEveryone() {
        stubAnonymous();

        Dancer anyDancer = candidate(2L, "AnyDancer", Sex.FEMALE, 1990, amsterdam);
        anyDancer.addDancestyle(latin, follow, intermediate);

        List<SearchService.SearchResult> results = searchService.match(params(List.of(), 100, 0, 7, 100), List.of(anyDancer));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("AnyDancer");
    }

    // ------------------------- role / sex / skilllevel -------------------------

    @Test
    void filtersOnRoleSexAndSkilllevel() {
        stubAnonymous();

        Dancer leadBeginnerMale = candidate(2L, "LeadBeginner", Sex.MALE, 1990, amsterdam);
        leadBeginnerMale.addDancestyle(ballroom, lead, beginner);
        Dancer followIntermediateFemale = candidate(3L, "FollowIntermediate", Sex.FEMALE, 1990, amsterdam);
        followIntermediateFemale.addDancestyle(ballroom, follow, intermediate);

        SearchService.SearchParameters criteria = params(
                List.of(style(ballroom, follow, SearchCriteriaSex.FEMALE, novice, intermediate)),
                100, 0, 7, 100);

        List<SearchService.SearchResult> results =
                searchService.match(criteria, List.of(leadBeginnerMale, followIntermediateFemale));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("FollowIntermediate");
    }

    @Test
    void skilllevelOutsideRangeIsExcluded() {
        stubAnonymous();

        Dancer tooLow = candidate(2L, "TooLow", Sex.FEMALE, 1990, amsterdam);
        tooLow.addDancestyle(ballroom, follow, beginner); // level 1
        Dancer inRange = candidate(3L, "InRange", Sex.FEMALE, 1990, amsterdam);
        inRange.addDancestyle(ballroom, follow, intermediate); // level 3

        SearchService.SearchParameters criteria = params(
                List.of(style(ballroom, follow, SearchCriteriaSex.EITHER, novice, intermediate)), // 2..3
                100, 0, 7, 100);

        List<SearchService.SearchResult> results = searchService.match(criteria, List.of(tooLow, inRange));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("InRange");
    }

    // ------------------------- age / distance / frequency -------------------------

    @Test
    void excludesSelfWhenLoggedIn() {
        Dancer current = dancer(1L, "Me", Sex.MALE, yearOfBirth(30), amsterdam);
        stubLoggedIn(current);

        Dancer me = candidate(1L, "Me", Sex.MALE, yearOfBirth(30), amsterdam);
        Dancer other = candidate(2L, "Other", Sex.FEMALE, yearOfBirth(30), amsterdam);

        List<SearchService.SearchResult> results = searchService.match(params(List.of(), 100, 0, 7, 100), List.of(me, other));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("Other");
    }

    @Test
    void appliesAgeDistanceFilterWhenLoggedIn() {
        Dancer current = dancer(1L, "Me", Sex.MALE, yearOfBirth(30), amsterdam);
        stubLoggedIn(current);

        Dancer near = candidate(2L, "Near", Sex.FEMALE, yearOfBirth(35), amsterdam); // diff 5
        Dancer far = candidate(3L, "Far", Sex.FEMALE, yearOfBirth(65), amsterdam);    // diff 35

        SearchService.SearchParameters strict = params(List.of(), 5, 0, 7, 100);
        assertThat(searchService.match(strict, List.of(near, far)))
                .extracting(r -> r.dancer().name())
                .containsExactly("Near");

        SearchService.SearchParameters loose = params(List.of(), 50, 0, 7, 100);
        assertThat(searchService.match(loose, List.of(near, far)))
                .extracting(r -> r.dancer().name())
                .containsExactlyInAnyOrder("Near", "Far");
    }

    @Test
    void appliesDistanceFilterWhenLoggedInAndSortsByDistance() {
        Dancer current = dancer(1L, "Me", Sex.MALE, yearOfBirth(30), amsterdam);
        stubLoggedIn(current);

        Dancer near = candidate(2L, "Near", Sex.FEMALE, yearOfBirth(30), amsterdam);  // ~0 km
        Dancer far = candidate(3L, "Far", Sex.FEMALE, yearOfBirth(30), rotterdam);    // ~57 km

        SearchService.SearchParameters close = params(List.of(), 100, 0, 7, 10);
        assertThat(searchService.match(close, List.of(near, far)))
                .extracting(r -> r.dancer().name())
                .containsExactly("Near");

        SearchService.SearchParameters wide = params(List.of(), 100, 0, 7, 200);
        assertThat(searchService.match(wide, List.of(far, near))) // deliberately unordered input
                .extracting(r -> r.dancer().name())
                .containsExactly("Near", "Far");
    }

    @Test
    void candidateWithoutCityIsIncludedFromAnyDistance() {
        Dancer current = dancer(1L, "Me", Sex.MALE, yearOfBirth(30), amsterdam);
        stubLoggedIn(current);

        Dancer noCity = candidate(2L, "NoCity", Sex.FEMALE, yearOfBirth(30), null);

        SearchService.SearchParameters tiny = params(List.of(), 100, 0, 7, 1);
        assertThat(searchService.match(tiny, List.of(noCity)))
                .extracting(r -> r.dancer().name())
                .containsExactly("NoCity");
    }

    @Test
    void appliesWeekFrequencyFilterRegardlessOfLogin() {
        stubAnonymous();

        Dancer overlaps = candidate(2L, "Overlaps", Sex.FEMALE, 1990, amsterdam).weekFrequencyMin(2).weekFrequencyMax(4);
        Dancer tooRare = candidate(3L, "TooRare", Sex.FEMALE, 1990, amsterdam).weekFrequencyMin(0).weekFrequencyMax(1);
        Dancer tooOften = candidate(4L, "TooOften", Sex.FEMALE, 1990, amsterdam).weekFrequencyMin(6).weekFrequencyMax(7);

        SearchService.SearchParameters criteria = params(List.of(), 100, 2, 5, 100);

        List<SearchService.SearchResult> results = searchService.match(criteria, List.of(overlaps, tooRare, tooOften));

        assertThat(results).extracting(r -> r.dancer().name()).containsExactly("Overlaps");
    }

    // ------------------------- haversine -------------------------

    @Test
    void haversineKmReturnsPlausibleDistance(){
        double km = SearchService.haversineKm(52.3676, 4.9041, 51.9225, 4.47917);
        assertThat(km).isCloseTo(57.0, org.assertj.core.data.Offset.offset(3.0));
    }

    // ------------------------- helpers -------------------------

    private static int yearOfBirth(int age) {
        return Year.now().getValue() - age;
    }

    private void stubAnonymous() {
        when(securityService.currentDancer()).thenReturn(Optional.empty());
    }

    private void stubLoggedIn(Dancer current) {
        when(securityService.currentDancer()).thenReturn(Optional.of(current));
        when(dancerRepository.findById(current.id())).thenReturn(Optional.of(current));
    }

    private Dancer candidate(long id, String name, Sex sex, int yearOfBirth, City city) {
        return new Dancer()
                .id(id)
                .name(name)
                .sex(sex)
                .yearOfBirth(yearOfBirth)
                .city(city);
    }

    private Dancer dancer(long id, String name, Sex sex, int yearOfBirth, City city) {
        return candidate(id, name, sex, yearOfBirth, city);
    }

    private static Dancestyle dancestyle(long id, String name) {
        return new Dancestyle().id(id).name(name);
    }

    private static Role role(long id, String name) {
        return new Role().id(id).name(name);
    }

    private static Skilllevel skilllevel(long id, String code, int level) {
        return new Skilllevel().id(id).code(code).level(level);
    }

    private static City city(long id, String name, double lat, double lon) {
        return new City().id(id).name(name).lat(lat).lon(lon);
    }

    private static Styles style(Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Skilllevel min, Skilllevel max) {
        return new Styles(dancestyle, role, sex, min, max);
    }

    private static Params params(List<SearchService.SearchParametersStyles> styles, int ageMax, int weekMin, int weekMax, int distMax) {
        return new Params(styles, ageMax, weekMin, weekMax, distMax);
    }

    private record Styles(
            Dancestyle dancestyle,
            Role role,
            SearchCriteriaSex sex,
            Skilllevel skilllevelMin,
            Skilllevel skilllevelMax) implements SearchService.SearchParametersStyles {
    }

    private record Params(
            List<SearchService.SearchParametersStyles> searchingFor,
            int ageDistanceMax,
            int weekFrequencyMin,
            int weekFrequencyMax,
            int distanceMax) implements SearchService.SearchParameters {
    }
}