package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.error.ShouldNotContain;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchResultCard;
import org.tbee.giwth.Then;

public class SearchResult {

    public static ShouldContain shouldContain(String email) {
        return new ShouldContain(email);
    }
    public static class ShouldContain implements Then<StepContext> {
        private final String email;

        public ShouldContain(String email) {
            this.email = email;
        }

        @Override
        public void run(StepContext sc) {
            Dancer dancer = sc.dancerRepository().findByEmail(email).orElseThrow();
            PlaywrightAssertions.assertThat(sc.page.locator("#" + SearchResultCard.class.getSimpleName() + "-" + dancer.id())).hasCount(1);
        }
    }

    public static ShouldNotContain shouldNotContain(String email) {
        return new ShouldNotContain(email);
    }
    public static class ShouldNotContain implements Then<StepContext> {
        private final String email;

        public ShouldNotContain(String email) {
            this.email = email;
        }

        @Override
        public void run(StepContext sc) {
            Dancer dancer = sc.dancerRepository().findByEmail(email).orElseThrow();
            PlaywrightAssertions.assertThat(sc.page.locator("#" + SearchResultCard.class.getSimpleName() + "-" + dancer.id())).hasCount(0);
        }
    }
}
