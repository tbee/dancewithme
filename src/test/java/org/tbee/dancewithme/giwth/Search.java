package org.tbee.dancewithme.giwth;

import org.tbee.giwth.Given;
import org.tbee.giwth.When;

public class Search {

    public static SearchingFor on() {
        return new SearchingFor();
    }
    public static class SearchingFor implements When<StepContext> {

        private Integer weekFrequencyMin;
        private Integer weekFrequencyMax;

        public SearchingFor weekFrequencyMin(int v) {
            this.weekFrequencyMin = v;
            return this;
        }

        public SearchingFor weekFrequencyMax(int v) {
            this.weekFrequencyMax = v;
            return this;
        }

        @Override
        public void run(StepContext sc) {
            sc.page.navigate(sc.baseUrl);

            if (weekFrequencyMin != null) {
                sc.page.locator("#weekFrequencyMinField").locator("input").fill(""  + weekFrequencyMin);
            }
            if (weekFrequencyMax != null) {
                sc.page.locator("#weekFrequencyMaxField").locator("input").fill(""  + weekFrequencyMax);
            }

            sc.page.locator("#searchButton").click();

//            sc.waitForMainLayoutToBecomeVisible();
        }
    }
}
