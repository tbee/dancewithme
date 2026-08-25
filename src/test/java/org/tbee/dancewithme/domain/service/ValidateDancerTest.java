package org.tbee.dancewithme.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateDancerTest {

    private ValidateDancer validateDancer;

    private final Dancestyle ballroom = new Dancestyle().id(1L).name("Ballroom");
    private final Dancestyle latin = new Dancestyle().id(2L).name("Latin");
    private final Role lead = new Role().id(1L).name("lead");
    private final Role follow = new Role().id(2L).name("follow");
    private final Skilllevel beginner = new Skilllevel().id(1L).code("absolute_beginner").level(1);
    private final Skilllevel intermediate = new Skilllevel().id(3L).code("intermediate_social").level(3);

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
        dancer.addDancestyle(ballroom, follow, beginner);
        dancer.addDancestyle(ballroom, lead, intermediate);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).contains(ValidateDancer.Problem.DUPLICATE_DANCESTYLE);
    }

    @Test
    void duplicateSearchingForIsReported() {
        Dancer dancer = new Dancer();
        dancer.addSearchingFor(latin, SearchCriteriaSex.FEMALE, follow, beginner, intermediate);
        dancer.addSearchingFor(latin, SearchCriteriaSex.FEMALE, lead, beginner, intermediate);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).contains(ValidateDancer.Problem.DUPLICATE_SEARCHING_FOR);
    }

    @Test
    void distinctDancestylesProduceNoDuplicateProblems() {
        Dancer dancer = new Dancer();
        dancer.addDancestyle(ballroom, follow, beginner);
        dancer.addDancestyle(latin, lead, intermediate);

        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, null, null, false);

        assertThat(problems).doesNotContain(
                ValidateDancer.Problem.DUPLICATE_DANCESTYLE,
                ValidateDancer.Problem.DUPLICATE_SEARCHING_FOR);
    }
}