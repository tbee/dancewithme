package org.tbee.dancewithme.giwth;

import org.tbee.giwth.Given;

public class Dancer {

    public static IsLoggedIn isLoggedIn(String email) {
        return new IsLoggedIn(email);
    }

    public static class IsLoggedIn implements Given<StepContext> {

        private final String email;

        public IsLoggedIn(String email) {
            this.email = email;
        }

        @Override
        public void run(StepContext sc) {
            sc.page.navigate(sc.baseUrl);

            sc.page.locator("#username").fill(email);
            sc.page.locator("#password").fill(email);
            sc.page.locator("button").click();

            sc.waitForMainLayoutToBecomeVisible();
        }
    }
}
