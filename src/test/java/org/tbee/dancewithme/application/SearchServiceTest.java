package org.tbee.dancewithme.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
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

    private static Dancer dancer(long id, String name, Sex sex, int ageMax, int weekMin, int weekMax, int distMax, List<DancerSearchingFor> styles) {
        Dancer dancer = new Dancer()
                .id(id)
                .name("Dancer " + id)
                .sex(sex)
                .ageDistanceMax(ageMax)
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
}