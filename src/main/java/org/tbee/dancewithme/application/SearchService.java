package org.tbee.dancewithme.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.SearchCriteriaSex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.Year;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchService {

    public record SearchStyleCriteria(
            Dancestyle dancestyle,
            Role role,
            SearchCriteriaSex sex,
            Skilllevel skilllevelMin,
            Skilllevel skilllevelMax) {
    }

    public record SearchCriteria(
            // matched or-ed: any of the selected style entries matches
            List<SearchStyleCriteria> styles,
            // only applied for logged in users, relative to their own age
            Integer ageDistanceMax,
            Integer weekFrequencyMin,
            Integer weekFrequencyMax,
            // only applied for logged in users, measured from their own city
            Integer distanceMax) {
    }

    public record SearchResult(Dancer dancer, Double distanceKm) {
    }

    private final DancerRepository dancerRepository;

    public SearchService(DancerRepository dancerRepository) {
        this.dancerRepository = dancerRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(SearchCriteria criteria, Dancer currentDancer) {
        boolean loggedIn = currentDancer != null;
        List<Dancer> candidates = loggedIn
                ? dancerRepository.findByActiveTrue()
                : dancerRepository.findByActiveTrueAndPubliclyFindableTrue();

        int currentYear = Year.now().getValue();
        // re-attach the current dancer within this transaction, so the lazy city can be loaded
        City fromCity = loggedIn ? dancerRepository.findById(currentDancer.id()).orElseThrow().city() : null;

        return candidates.stream()
                // never include the searching dancer itself
                .filter(dancer -> !loggedIn || dancer.id() != currentDancer.id())
                .filter(dancer -> criteria.styles() == null || criteria.styles().isEmpty()
                        || dancer.dancestyles().stream().anyMatch(dd -> criteria.styles().stream().anyMatch(style ->
                                dd.dancestyle().equals(style.dancestyle())
                                        && (style.role() == null || dd.role().equals(style.role()))
                                        && (style.sex() == null || style.sex().match(dancer.sex()))
                                        && (style.skilllevelMin() == null || dd.skilllevel().level() >= style.skilllevelMin().level())
                                        && (style.skilllevelMax() == null || dd.skilllevel().level() <= style.skilllevelMax().level()))))
                .filter(dancer -> !loggedIn || criteria.ageDistanceMax() == null
                        || Math.abs(age(dancer, currentYear) - age(currentDancer, currentYear)) <= criteria.ageDistanceMax())
                .filter(dancer -> criteria.weekFrequencyMin() == null || dancer.weekFrequencyMax() >= criteria.weekFrequencyMin())
                .filter(dancer -> criteria.weekFrequencyMax() == null || dancer.weekFrequencyMin() <= criteria.weekFrequencyMax())
                .map(dancer -> new SearchResult(dancer, distanceKm(fromCity, dancer.city())))
                .filter(result -> !loggedIn || criteria.distanceMax() == null || result.distanceKm() == null || result.distanceKm() <= criteria.distanceMax())
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
