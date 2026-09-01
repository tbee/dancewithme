package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
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
                    selectComboBoxItem(sc, searchingForRow.locator("#styleComboBox"), searchingForStyle.dancestyle, true);
                }

                if (searchingForStyle.skilllevelMin != null) {
                    selectComboBoxItem(sc, searchingForRow.locator("#skilllevelMinComboBox"), searchingForStyle.skilllevelMin + " - ", false);
                }

                if (searchingForStyle.skilllevelMax != null) {
                    selectComboBoxItem(sc, searchingForRow.locator("#skilllevelMaxComboBox"), searchingForStyle.skilllevelMax + " - ", false);
                }
            }
            sc.page.locator("#searchButton").click();
        }
    }

    public static class SearchingForStyle implements When<StepContext> {
        private final SearchingFor searchingFor;
        private String dancestyle;
        private Integer skilllevelMin;
        private Integer skilllevelMax;

        SearchingForStyle(SearchingFor searchingFor) {
            this.searchingFor = searchingFor;
        }

        public SearchingForStyle dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public SearchingForStyle skilllevelMin(int v) {
            this.skilllevelMin = v;
            return this;
        }

        public SearchingForStyle skilllevelMax(int v) {
            this.skilllevelMax = v;
            return this;
        }
        // TOOD:
        //        roleSelect = new RoleSelect();
        //        searchCriteriaSexComboBox = new SearchCriteriaSexComboBox();
        //        skilllevelMinComboBox = new SkilllevelComboBox();
        //        skilllevelMaxComboBox = new SkilllevelComboBox();

        @Override
        public void run(StepContext sc) {
            searchingFor.run(sc);
        }

        // chain new
        public SearchingForStyle or() {
            return searchingFor.style();
        }

        public SearchingFor also() {
            return searchingFor;
        }
    }


    // -------


    private static void selectComboBoxItem(StepContext sc, Locator comboBox, String text, boolean exact) {
        comboBox.locator("#toggleButton").click();
        comboBox.locator("vaadin-combo-box-item")
                .getByText(text, new Locator.GetByTextOptions().setExact(exact))
                .click();
    }
}
