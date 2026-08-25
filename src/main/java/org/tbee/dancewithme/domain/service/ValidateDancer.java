package org.tbee.dancewithme.domain.service;

import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Validates the domain rules a {@link Dancer} must satisfy before it can be persisted.
 * This is pure domain logic: it does not depend on the UI or on persistence.
 */
public class ValidateDancer {

    public static final int MIN_PASSWORD_LENGTH = 8;

    /** The individual problems that can make a dancer invalid. */
    public enum Problem {
        PASSWORD_TOO_SHORT,
        PASSWORDS_DO_NOT_MATCH,
        PRIVACY_AGREEMENT_REQUIRED,
        DUPLICATE_DANCESTYLE,
        DUPLICATE_SEARCHING_FOR
    }

    /**
     * Validates the dancer and returns the list of problems found (empty when valid).
     *
     * @param dancer                  the dancer to validate
     * @param rawPassword             the un-hashed password, or {@code null} for the update use case
     *                                (in which case the password and privacy checks are skipped)
     * @param rawPasswordConfirmation the repeated password, or {@code null} for the update use case
     * @param privacyAccepted         whether the privacy agreement has been accepted
     */
    public List<Problem> validate(Dancer dancer, String rawPassword, String rawPasswordConfirmation, boolean privacyAccepted) {
        List<Problem> problems = new ArrayList<>();

        if (rawPassword != null && rawPassword.length() < MIN_PASSWORD_LENGTH) {
            problems.add(Problem.PASSWORD_TOO_SHORT);
        }
        if (rawPassword != null && !rawPassword.equals(rawPasswordConfirmation)) {
            problems.add(Problem.PASSWORDS_DO_NOT_MATCH);
        }
        if (rawPassword != null && !privacyAccepted) {
            problems.add(Problem.PRIVACY_AGREEMENT_REQUIRED);
        }

        if (hasDuplicateDancestyle(dancer.dancestyles(), DancerDancestyle::dancestyle)) {
            problems.add(Problem.DUPLICATE_DANCESTYLE);
        }
        if (hasDuplicateDancestyle(dancer.searchingFor(), DancerSearchingFor::dancestyle)) {
            problems.add(Problem.DUPLICATE_SEARCHING_FOR);
        }

        return problems;
    }

    private static <T> boolean hasDuplicateDancestyle(List<T> entries, Function<T, Dancestyle> dancestyleOf) {
        Set<Dancestyle> seen = new HashSet<>();
        for (T entry : entries) {
            Dancestyle dancestyle = dancestyleOf.apply(entry);
            if (dancestyle != null && !seen.add(dancestyle)) {
                return true;
            }
        }
        return false;
    }
}