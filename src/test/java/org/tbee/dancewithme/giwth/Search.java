package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.infrastructure.vdn.component.DancestyleComboBox;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchingForRow;
import org.tbee.giwth.When;

import java.util.ArrayList;
import java.util.List;

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

        public SearchingForStyle style() {
            SearchingForStyle searchingForStyle = new SearchingForStyle(this);
            searchingForStyles.add(searchingForStyle);
            return searchingForStyle;
        }
        private final List<SearchingForStyle> searchingForStyles = new ArrayList<>();

        // "addSearchingForStyleButton"

        @Override
        public void run(StepContext sc) {
            sc.page.navigate(sc.baseUrl);

            if (weekFrequencyMin != null) {
                sc.page.locator("#weekFrequencyMinField").locator("input").fill(""  + weekFrequencyMin);
            }
            if (weekFrequencyMax != null) {
                sc.page.locator("#weekFrequencyMaxField").locator("input").fill(""  + weekFrequencyMax);
            }

            for (int index = 0; index < searchingForStyles.size(); index++) {
                SearchingForStyle searchingForStyle = searchingForStyles.get(index);

                // Add row
                sc.page.locator("#addSearchingForStyleButton");
                Locator searchingForRow = sc.page.locator("#" + SearchingForRow.class.getSimpleName() + index);

                if (searchingForStyle.dancestyle != null) {
                    Locator dancestyleCombBox = searchingForRow.locator("#" + DancestyleComboBox.class.getSimpleName()); // find combobox
                    dancestyleCombBox.locator("#toggleButton").click(); // open dropdown
                    dancestyleCombBox.locator("vaadin-combo-box-item") // click correct entry
                            .getByText(searchingForStyle.dancestyle, new Locator.GetByTextOptions().setExact(true))
                            .click();
                }
            }
            sc.page.locator("#searchButton").click();
        }
    }

    public static class SearchingForStyle implements When<StepContext> {
        private final SearchingFor searchingFor;
        private String dancestyle;

        SearchingForStyle(SearchingFor searchingFor) {
            this.searchingFor = searchingFor;
        }

        public SearchingForStyle dancestyle(String dancestyle) {
            this.dancestyle = dancestyle;
            return this;
        }

        @Override
        public void run(StepContext sc) {
            searchingFor.run(sc);
        }
    }

    // -------

}
