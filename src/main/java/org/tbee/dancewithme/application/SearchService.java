package org.tbee.dancewithme.application;

import org.springframework.stereotype.Service;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.time.Year;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchService {

    public interface SearchParametersStyles {
        Dancestyle dancestyle();
        SearchCriteriaSex sex();
        Role role();
        int skilllevelMin();
        int skilllevelMax();
    }

    public interface SearchParameters {
        int weekFrequencyMax();
        List<? extends SearchParametersStyles> searchingFor();

        // only applied for logged in users
        int distanceMax();
        int weekFrequencyMin();
    }

    public record SearchResult(Dancer dancer, Double distanceKm) {
    }

    private final DancerRepository dancerRepository;
    private final SecurityService securityService;

    public SearchService(DancerRepository dancerRepository, SecurityService securityService) {
        this.dancerRepository = dancerRepository;
        this.securityService = securityService;
    }

    public List<SearchResult> search(SearchParameters criteria) {
        boolean loggedIn = securityService.loggedInDancer().isPresent();
        List<Dancer> candidates = loggedIn
                ? dancerRepository.findByActiveTrue()
                : dancerRepository.findByActiveTrueAndPubliclyFindableTrue();
        return match(criteria, candidates, securityService.loggedInDancer().orElse(null));
    }

    public List<SearchResult> match(SearchParameters criteria, List<Dancer> candidates, Dancer loggedInDancer) {
        boolean loggedIn = (loggedInDancer != null);
        City fromCity = (loggedIn ? loggedInDancer.city() : null);

        return candidates.stream()
                // never include the searching dancer itself
                .filter(dancer -> !loggedIn || dancer.id() != loggedInDancer.id())
                // dance frequency should match
                .filter(dancer -> dancer.weekFrequencyMax() >= criteria.weekFrequencyMin())
                .filter(dancer -> dancer.weekFrequencyMin() <= criteria.weekFrequencyMax())
                // dancestyle should match
                .filter(dancer -> criteria.searchingFor() == null || criteria.searchingFor().isEmpty() || // no criteria is always match
                        criteria.searchingFor().stream().anyMatch(searchingFor -> // all criteria
                                dancer.dancestyles().stream().anyMatch(dancerDancestyle -> // match again all dancestyles
                                        (searchingFor.dancestyle() == null || searchingFor.dancestyle().equals(dancerDancestyle.dancestyle())) &&
                                        (searchingFor.role() == null || searchingFor.role().equals(dancerDancestyle.role())) &&
                                        (searchingFor.sex() == null || searchingFor.sex().match(dancer.sex())) &&
                                        (searchingFor.skilllevelMin() <= dancerDancestyle.skilllevel()) &&
                                        (searchingFor.skilllevelMax() >= dancerDancestyle.skilllevel())
                                )
                        )
                )
                // Convert to result (so distance in km is calculated and snapshotted)
                .map(dancer -> new SearchResult(dancer, distanceKm(fromCity, dancer.city())))
                // dancer should be in maximum distance (unknown distance is kept, sorted last)
                .filter(result -> !loggedIn || result.distanceKm() == null || result.distanceKm() <= criteria.distanceMax())
                .sorted(Comparator.comparing(SearchResult::distanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private static Double distanceKm(City from, City to) {
        if (from == null || to == null) {
            return null;
        }
        return haversineKm(from.lat(), from.lon(), to.lat(), to.lon());
    }

    // https://en.wikipedia.org/wiki/Haversine_formula
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
