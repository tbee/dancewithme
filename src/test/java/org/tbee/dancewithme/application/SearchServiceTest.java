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
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.valueobject.Role;
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
import static org.tbee.dancewithme.domain.valueobject.Role.FOLLOW;
import static org.tbee.dancewithme.domain.valueobject.Role.LEAD;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DancerRepository dancerRepository;
    @Mock
    private SecurityService securityService;

    private SearchService searchService;

    private final Dancestyle ballroom = dancestyle(1L, "Ballroom");
    private final Dancestyle latin = dancestyle(2L, "Latin");
    private final int beginner1 = 1;
    private final int novice2 = 2;
    private final int intermediate3 = 3;
    private final int advanced4 = 4;
    private final int preCompetition5 = 5;
    private final int regional6 = 6;
    private final int national7 = 7;
    private final int nationalFinalist8 = 8;
    private final int international9 = 9;
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
        Dancer candidate = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100);
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
        Dancer near = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100);
        Dancer far = dancer(2L, Sex.FEMALE, berlin, 0, 7, 100);
        when(dancerRepository.findByActiveTrueAndPubliclyFindableTrue()).thenReturn(List.of(near, far));

        Dancer criteria = dancer(0L, "anonymous", Sex.UNKNOWN, 0, 7, 1, List.of());

        List<SearchService.SearchResult> result = searchService.search(criteria);

        assertEquals(2, result.size());
    }

    @Test
    void searchLoggedInExcludesSelfAndUsesActiveRepository() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100);
        when(securityService.loggedInDancer()).thenReturn(Optional.of(searcher));
        Dancer other = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100);
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
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 2, 4, 100);
        Dancer overlapLow = dancer(1L, Sex.FEMALE, amsterdam, 1, 2, 100);
        Dancer overlapHigh = dancer(2L, Sex.FEMALE, amsterdam, 4, 6, 100);
        Dancer tooLow = dancer(3L, Sex.FEMALE, amsterdam, 0, 1, 100);
        Dancer tooHigh = dancer(4L, Sex.FEMALE, amsterdam, 5, 7, 100);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(overlapLow, overlapHigh, tooLow, tooHigh), searcher);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.dancer().id() == overlapLow.id()));
        assertTrue(result.stream().anyMatch(r -> r.dancer().id() == overlapHigh.id()));
    }

    @Test
    void matchWithoutSearchingForMatchesAllStyles() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100);
        Dancer withStyle = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, LEAD, intermediate3)));
        Dancer withoutStyle = dancer(2L, Sex.FEMALE, amsterdam, 0, 7, 100);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(withStyle, withoutStyle), searcher);

        assertEquals(2, result.size());
    }

    @Test
    void matchFiltersOnDancestyle() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100)
                .searchingFor(List.of(searchingFor(ballroom, LEAD, SearchCriteriaSex.EITHER, beginner1, intermediate3)));

        Dancer match = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, LEAD, novice2)));
        Dancer wrongStyle = dancer(2L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(latin, LEAD, novice2)));
        Dancer wrongRole = dancer(3L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, FOLLOW, novice2)));
        Dancer tooSkilled = dancer(4L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, LEAD, advanced4)));

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(match, wrongStyle, wrongRole, tooSkilled), searcher);

        assertEquals(1, result.size());
        assertEquals(match.id(), result.getFirst().dancer().id());
    }

    @Test
    void matchFiltersOnSex() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100)
                    .searchingFor(List.of(searchingFor(ballroom, LEAD, SearchCriteriaSex.FEMALE, beginner1, intermediate3)));

        Dancer female = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, LEAD, novice2)));
        Dancer male = dancer(2L, Sex.MALE, amsterdam, 0, 7, 100).dancestyles(List.of(dancerDancestyle(ballroom, LEAD, novice2)));

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(female, male), searcher);

        assertEquals(1, result.size());
        assertEquals(female.id(), result.get(0).dancer().id());
    }

    @Test
    void matchFiltersOnDistanceAndSortsAscending() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100);
        Dancer same = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100);
        Dancer close = dancer(2L, Sex.FEMALE, rotterdam, 0, 7, 100);
        Dancer far = dancer(3L, Sex.FEMALE, berlin, 0, 7, 100);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(far, close, same), searcher);

        assertEquals(2, result.size());
        assertEquals(same.id(), result.get(0).dancer().id());
        assertEquals(close.id(), result.get(1).dancer().id());
        assertTrue(result.get(0).distanceKm() <= result.get(1).distanceKm());
    }

    @Test
    void matchYieldsNullDistanceForAnonymous() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100);
        Dancer noCity = dancer(1L, Sex.FEMALE, amsterdam, 0, 7, 100);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(noCity), null);

        assertEquals(1, result.size());
        assertEquals(null, result.getFirst().distanceKm());
    }

    @Test
    void matchIncludesNullDistanceCandidateWhenLoggedIn() {
        Dancer searcher = dancer(99L, Sex.MALE, amsterdam, 0, 7, 100);
        Dancer noCity = dancer(1L, Sex.FEMALE, null, 0, 7, 100);
        Dancer withCity = dancer(2L, Sex.FEMALE, amsterdam, 0, 7, 100);

        List<SearchService.SearchResult> result = searchService.match(searcher, List.of(noCity, withCity), searcher);

        assertEquals(2, result.size());
        assertEquals(withCity.id(), result.get(0).dancer().id());
        assertEquals(noCity.id(), result.get(1).dancer().id());
        assertEquals(null, result.get(1).distanceKm());
    }

    @Test
    void tomMarijke() {
        Dancer tom = dancer(1L, Sex.MALE, amsterdam, 2, 3, 100)
                .dancestyles(List.of(
                        dancerDancestyle(ballroom, LEAD, national7),
                        dancerDancestyle(latin, LEAD, regional6)
                ))
                .searchingFor(List.of(
                        searchingFor(ballroom, FOLLOW, SearchCriteriaSex.FEMALE, preCompetition5, international9),
                        searchingFor(latin, FOLLOW, SearchCriteriaSex.FEMALE, advanced4, national7)
                ));

        Dancer marijke = dancer(2L, Sex.FEMALE, rotterdam, 1, 2, 100)
                .dancestyles(List.of(
                        dancerDancestyle(ballroom, FOLLOW, national7),
                        dancerDancestyle(latin, FOLLOW, regional6)
                ))
                .searchingFor(List.of(
                        searchingFor(ballroom, LEAD, SearchCriteriaSex.MALE, advanced4, international9),
                        searchingFor(latin, LEAD, SearchCriteriaSex.MALE, advanced4, national7)
                ));

        assertEquals(1, searchService.match(tom, List.of(marijke), tom).size());
        assertEquals(1, searchService.match(marijke, List.of(tom), marijke).size());
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

    private static DancerSearchingFor searchingFor(Dancestyle dancestyle, Role role, SearchCriteriaSex sex, int min, int max) {
        return new DancerSearchingFor()
                .dancestyle(dancestyle)
                .sex(sex)
                .role(role)
                .skilllevelMin(min)
                .skilllevelMax(max);
    }

    private static DancerDancestyle dancerDancestyle(Dancestyle dancestyle, Role role, int skilllevel) {
        return new DancerDancestyle()
                .dancestyle(dancestyle)
                .role(role)
                .skilllevel(skilllevel);
    }

    private Dancer dancer(long id, Sex sex, City city, int weekMin, int weekMax, int distMax) {
        return dancer(id, "Dancer " + id, sex, weekMin, weekMax, distMax, List.of())
                .city(city);
    }
}