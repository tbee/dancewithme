package org.tbee.dancewithme.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {

    public interface SearchParametersStyles {
            Dancestyle dancestyle();
            Role role();
            SearchCriteriaSex sex();
            Skilllevel skilllevelMin();
            Skilllevel skilllevelMax();
    }

    public interface SearchParameters {
            // matched or-ed: any of the selected style entries matches
            List<? extends SearchParametersStyles> searchingFor();
            // only applied for logged in users, relative to their own age
            int ageDistanceMax();
            int weekFrequencyMin();
            int weekFrequencyMax();
            // only applied for logged in users, measured from their own city
            int distanceMax();
    }

    public record SearchResult(Dancer dancer, Double distanceKm) {
    }

    private final DancerRepository dancerRepository;
    private final SecurityService securityService;

    public SearchService(DancerRepository dancerRepository, SecurityService securityService) {
        this.dancerRepository = dancerRepository;
        this.securityService = securityService;
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(SearchParameters criteria) {
        boolean loggedIn = securityService.currentDancer().isPresent();
        List<Dancer> candidates = loggedIn
                ? dancerRepository.findByActiveTrue()
                : dancerRepository.findByActiveTrueAndPubliclyFindableTrue();
        return match(criteria, candidates);
    }

    @Transactional(readOnly = true)
    public List<SearchResult> match(SearchParameters criteria, List<Dancer> candidates) {
        Dancer currentDancer = securityService.currentDancer().orElse(null);
        boolean loggedIn = (currentDancer != null);

        int currentYear = Year.now().getValue();
        // re-attach the current dancer within this transaction, so the lazy city can be loaded
        City fromCity = loggedIn ? dancerRepository.findById(currentDancer.id()).orElseThrow().city() : null;

        return candidates.stream()
                // never include the searching dancer itself
                .filter(dancer -> !loggedIn || dancer.id() != currentDancer.id())
                .filter(dancer -> criteria.searchingFor() == null || criteria.searchingFor().isEmpty()
                        || dancer.dancestyles().stream().anyMatch(dd -> criteria.searchingFor().stream().anyMatch(style ->
                        dd.dancestyle().equals(style.dancestyle())
                                && (style.role() == null || dd.role().equals(style.role()))
                                && (style.sex() == null || style.sex().match(dancer.sex()))
                                && (style.skilllevelMin() == null || dd.skilllevel().level() >= style.skilllevelMin().level())
                                && (style.skilllevelMax() == null || dd.skilllevel().level() <= style.skilllevelMax().level()))))
                .filter(dancer -> !loggedIn
                        || Math.abs(age(dancer, currentYear) - age(currentDancer, currentYear)) <= criteria.ageDistanceMax())
                .filter(dancer -> dancer.weekFrequencyMax() >= criteria.weekFrequencyMin())
                .filter(dancer -> dancer.weekFrequencyMin() <= criteria.weekFrequencyMax())
                .map(dancer -> new SearchResult(dancer, distanceKm(fromCity, dancer.city())))
                .filter(result -> !loggedIn || result.distanceKm() == null || result.distanceKm() <= criteria.distanceMax())
                .sorted(Comparator.comparing(SearchResult::distanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                // initialize the lazy relations needed by the views (open-in-view is disabled)
                .peek(result -> {
                    Dancer dancer = result.dancer();
                    dancer.whoami();
                    dancer.mugshot();
                    if (dancer.city() != null) {
                        dancer.city().name();
                    }
                    dancer.dancestyles().forEach(dd -> {
                        dd.dancestyle().name();
                        dd.role().name();
                    });
                })
                .toList();
    }

    private static int age(Dancer dancer, int currentYear) {
        return currentYear - dancer.yearOfBirth();
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
