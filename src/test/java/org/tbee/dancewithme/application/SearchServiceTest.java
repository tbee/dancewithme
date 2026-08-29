package org.tbee.dancewithme.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private final City berlin = city(3L, "Berlin", 52.5200, 13.4050);

    @BeforeEach
    void setUp() {
        searchService = new SearchService(dancerRepository, securityService);
    }


    // ------------------------- search -------------------------

    @Test
    void searchAnonymousUsesPubliclyFindableRepository() {
        stubAnonymous();
        Dancer candidate = candidate(1L, amsterdam, 0, 7, null);
        when(dancerRepository.findByActiveTrueAndPubliclyFindableTrue()).thenReturn(List.of(candidate));

        Dancer criteria = dancer(0L, "anonymous", Sex.UNKNOWN, 0, 7, 100, List.of());

        List<SearchService.SearchResult> result = searchService.search(criteria);

        assertEquals(1, result.size());
        assertEquals(candidate.id(), result.get(0).dancer().id());
        verify(dancerRepository).findByActiveTrueAndPubliclyFindableTrue();
        verify(dancerRepository, never()).findByActiveTrue();
    }

    @Test
    void searchAnonymousDoesNotApplyDistanceFilter() {
        stubAnonymous();
        Dancer near = candidate(1L, amsterdam, 0, 7, null);
        Dancer far = candidate(2L, berlin, 0, 7, null);
        when(dancerRepository.findByActiveTrueAndPubliclyFindableTrue()).thenReturn(List.of(near, far));

        Dancer criteria = dancer(0L, "anonymous", Sex.UNKNOWN, 0, 7, 1, List.of());

        List<SearchService.SearchResult> result = searchService.search(criteria);

        assertEquals(2, result.size());
    }

    @Test
    void searchLoggedInExcludesSelfAndUsesActiveRepository() {
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of());
        when(securityService.loggedInDancer()).thenReturn(Optional.of(searcher));
        Dancer other = candidate(1L, amsterdam, 0, 7, null);
        when(dancerRepository.findByActiveTrue()).thenReturn(List.of(searcher, other));

        List<SearchService.SearchResult> result = searchService.search(searcher);

        assertEquals(1, result.size());
        assertEquals(other.id(), result.getFirst().dancer().id());
        verify(dancerRepository).findByActiveTrue();
        verify(dancerRepository, never()).findByActiveTrueAndPubliclyFindableTrue();
    }


    // ------------------------- match -------------------------

    @Test
    void matchFiltersOnWeekFrequency() {
        Dancer searcher = searcher(amsterdam, 2, 4, 100, List.of());
        Dancer overlapLow = candidate(1L, amsterdam, 1, 2, null);
        Dancer overlapHigh = candidate(2L, amsterdam, 4, 6, null);
        Dancer tooLow = candidate(3L, amsterdam, 0, 1, null);
        Dancer tooHigh = candidate(4L, amsterdam, 5, 7, null);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(overlapLow, overlapHigh, tooLow, tooHigh), searcher);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.dancer().id() == overlapLow.id()));
        assertTrue(result.stream().anyMatch(r -> r.dancer().id() == overlapHigh.id()));
    }

    @Test
    void matchWithoutSearchingForMatchesAllStyles() {
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of());
        Dancer withStyle = candidate(1L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, lead, intermediate)));
        Dancer withoutStyle = candidate(2L, amsterdam, 0, 7, List.of());

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(withStyle, withoutStyle), searcher);

        assertEquals(2, result.size());
    }

    @Test
    void matchFiltersOnDancestyle() {
        DancerSearchingFor want = searchingFor(ballroom, lead, SearchCriteriaSex.EITHER, beginner, intermediate);
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of(want));

        Dancer match = candidate(1L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, lead, novice)));
        Dancer wrongStyle = candidate(2L, amsterdam, 0, 7, List.of(dancerDancestyle(latin, lead, novice)));
        Dancer wrongRole = candidate(3L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, follow, novice)));
        Dancer tooSkilled = candidate(4L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, lead, skilllevel(4L, "advanced", 4))));

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(match, wrongStyle, wrongRole, tooSkilled), searcher);

        assertEquals(1, result.size());
        assertEquals(match.id(), result.get(0).dancer().id());
    }

    @Test
    void matchFiltersOnSex() {
        DancerSearchingFor wantFemale = searchingFor(ballroom, lead, SearchCriteriaSex.FEMALE, beginner, intermediate);
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of(wantFemale));

        Dancer female = candidate(1L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, lead, novice))).sex(Sex.FEMALE);
        Dancer male = candidate(2L, amsterdam, 0, 7, List.of(dancerDancestyle(ballroom, lead, novice))).sex(Sex.MALE);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(female, male), searcher);

        assertEquals(1, result.size());
        assertEquals(female.id(), result.get(0).dancer().id());
    }

    @Test
    void matchFiltersOnDistanceAndSortsAscending() {
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of());
        Dancer same = candidate(1L, amsterdam, 0, 7, null);
        Dancer close = candidate(2L, rotterdam, 0, 7, null);
        Dancer far = candidate(3L, berlin, 0, 7, null);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(far, close, same), searcher);

        assertEquals(2, result.size());
        assertEquals(same.id(), result.get(0).dancer().id());
        assertEquals(close.id(), result.get(1).dancer().id());
        assertTrue(result.get(0).distanceKm() <= result.get(1).distanceKm());
    }

    @Test
    void matchYieldsNullDistanceForAnonymous() {
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of());
        Dancer noCity = candidate(1L, amsterdam, 0, 7, null);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(noCity), null);

        assertEquals(1, result.size());
        assertEquals(null, result.get(0).distanceKm());
    }

    @Test
    void matchIncludesNullDistanceCandidateWhenLoggedIn() {
        Dancer searcher = searcher(amsterdam, 0, 7, 100, List.of());
        Dancer noCity = candidate(1L, null, 0, 7, null);
        Dancer withCity = candidate(2L, amsterdam, 0, 7, null);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(noCity, withCity), searcher);

        assertEquals(2, result.size());
        assertEquals(withCity.id(), result.get(0).dancer().id());
        assertEquals(noCity.id(), result.get(1).dancer().id());
        assertEquals(null, result.get(1).distanceKm());
    }


    // ------------------------- haversineKm -------------------------

    @Test
    void haversineKmAmsterdamRotterdam() {
        double distance = SearchService.haversineKm(52.3676, 4.9041, 51.9225, 4.47917);
        assertTrue(distance > 50.0 && distance < 65.0, "unexpected distance: " + distance);
    }

    @Test
    void haversineKmSameLocationIsZero() {
        assertEquals(0.0, SearchService.haversineKm(52.3676, 4.9041, 52.3676, 4.9041));
    }


    // ------------------------- helpers -------------------------

    private static int yearOfBirth(int age) {
        return Year.now().getValue() - age;
    }

    private void stubAnonymous() {
        when(securityService.loggedInDancer()).thenReturn(Optional.empty());
    }

    private void stubLoggedIn(Dancer dancer) {
        when(securityService.loggedInDancer()).thenReturn(Optional.of(dancer));
        when(dancerRepository.findById(dancer.id())).thenReturn(Optional.of(dancer));
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

    private static Dancer dancer(long id, String name, Sex sex, int weekMin, int weekMax, int distMax, List<DancerSearchingFor> styles) {
        Dancer dancer = new Dancer()
                .id(id)
                .name(name)
                .sex(sex)
                .weekFrequencyMin(weekMin)
                .weekFrequencyMax(weekMax)
                .distanceMax(distMax)
                .searchingFor(styles);
        styles.forEach(s -> s.dancer(dancer));
        return dancer;
    }

    private static DancerSearchingFor searchingFor(Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Skilllevel min, Skilllevel max) {
        return new DancerSearchingFor()
                .dancestyle(dancestyle)
                .sex(sex)
                .role(role)
                .skilllevelMin(min)
                .skilllevelMax(max);
    }

    private static DancerDancestyle dancerDancestyle(Dancestyle dancestyle, Role role, Skilllevel skilllevel) {
        return new DancerDancestyle()
                .dancestyle(dancestyle)
                .role(role)
                .skilllevel(skilllevel);
    }

    private Dancer searcher(City city, int weekMin, int weekMax, int distMax, List<DancerSearchingFor> styles) {
        return dancer(99L, "Searcher", Sex.MALE, weekMin, weekMax, distMax, styles)
                .city(city);
    }

    private Dancer candidate(long id, City city, int weekMin, int weekMax, List<DancerDancestyle> styles) {
        Dancer dancer = dancer(id, "Dancer " + id, Sex.FEMALE, weekMin, weekMax, 100, List.of())
                .city(city);
        if (styles != null) {
            dancer.dancestyles(styles);
        }
        return dancer;
    }
}