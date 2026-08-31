package org.tbee.dancewithme.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tbee.dancewithme.domain.valueobject.Role.FOLLOW;
import static org.tbee.dancewithme.domain.valueobject.Role.LEAD;

class ValidateDancerTest {

    private ValidateDancer validateDancer;

    private final Dancestyle ballroom = new Dancestyle().id(1L).name("Ballroom");
    private final Dancestyle latin = new Dancestyle().id(2L).name("Latin");

    @BeforeEach
    void setUp() {
        validateDancer = new ValidateDancer();
    }

    @Test
    void emptyDancerProducesNoProblems() {
        Dancer dancer = new Dancer();

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).isEmpty();
    }

    @Test
    void tooShortPasswordIsReported() {
        Dancer dancer = new Dancer();

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, "short", "short", true);

        assertThat(problems).contains(ValidateDancer.Problem.PASSWORD_TOO_SHORT);
    }

    @Test
    void mismatchingPasswordsAreReported() {
        Dancer dancer = new Dancer();

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, "long-enough", "different", true);

        assertThat(problems).contains(ValidateDancer.Problem.PASSWORDS_DO_NOT_MATCH);
    }

    @Test
    void missingPrivacyAgreementIsReported() {
        Dancer dancer = new Dancer();

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, "long-enough", "long-enough", false);

        assertThat(problems).contains(ValidateDancer.Problem.PRIVACY_AGREEMENT_REQUIRED);
    }

    @Test
    void nullRawPasswordSkipsRegistrationChecks() {
        Dancer dancer = new Dancer();

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).isEmpty();
    }

    @Test
    void duplicateDancestylesAreReported() {
        Dancer dancer = new Dancer();
        dancer.addDancestyle(ballroom, FOLLOW, 1);
        dancer.addDancestyle(ballroom, LEAD, 3);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).contains(ValidateDancer.Problem.DUPLICATE_DANCESTYLE);
    }

    @Test
    void duplicateSearchingForIsReported() {
        Dancer dancer = new Dancer();
        dancer.addSearchingFor(latin, SearchCriteriaSex.FEMALE, FOLLOW, 1, 3);
        dancer.addSearchingFor(latin, SearchCriteriaSex.FEMALE, LEAD, 1, 3);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).contains(ValidateDancer.Problem.DUPLICATE_SEARCHING_FOR);
    }

    @Test
    void distinctDancestylesProduceNoDuplicateProblems() {
        Dancer dancer = new Dancer();
        dancer.addDancestyle(ballroom, FOLLOW, 1);
        dancer.addDancestyle(latin, LEAD, 3);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).doesNotContain(
                ValidateDancer.Problem.DUPLICATE_DANCESTYLE,
                ValidateDancer.Problem.DUPLICATE_SEARCHING_FOR);
    }
}