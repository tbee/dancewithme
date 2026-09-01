package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import org.tbee.dancewithme.infrastructure.vdn.component.CandoRow;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchingForRow;
import org.tbee.giwth.When;

import java.util.ArrayList;
import java.util.List;

public class Register {

    public static Form on() {
        return new Form();
    }

    public static class Form implements When<StepContext> {

        private String name;
        private String email;
        private String password;
        private String confirmPassword;
        private String city;
        private String sex;
        private String whoami;
        private String whatdoiwant;
        private Integer weekFrequencyMin;
        private Integer weekFrequencyMax;
        private Integer maxDistance;
        private Boolean active;
        private Boolean publiclyFindable;
        private Boolean privacyAgreement;

        public Form name(String v) {
            this.name = v;
            return this;
        }

        public Form email(String v) {
            this.email = v;
            return this;
        }

        public Form password(String v) {
            this.password = v;
            return this;
        }

        public Form confirmPassword(String v) {
            this.confirmPassword = v;
            return this;
        }

        public Form city(String v) {
            this.city = v;
            return this;
        }

        public Form sex(String v) {
            this.sex = v;
            return this;
        }

        public Form whoami(String v) {
            this.whoami = v;
            return this;
        }

        public Form whatdoiwant(String v) {
            this.whatdoiwant = v;
            return this;
        }

        public Form weekFrequencyMin(int v) {
            this.weekFrequencyMin = v;
            return this;
        }

        public Form weekFrequencyMax(int v) {
            this.weekFrequencyMax = v;
            return this;
        }

        public Form maxDistance(int v) {
            this.maxDistance = v;
            return this;
        }

        public Form active(boolean v) {
            this.active = v;
            return this;
        }

        public Form publiclyFindable(boolean v) {
            this.publiclyFindable = v;
            return this;
        }

        public Form privacyAgreement(boolean v) {
            this.privacyAgreement = v;
            return this;
        }

        public DancestyleStyle canDo() {
            DancestyleStyle style = new DancestyleStyle(this);
            canDo.add(style);
            return style;
        }
        private final List<DancestyleStyle> canDo = new ArrayList<>();

        public SearchingForStyle searchingFor() {
            SearchingForStyle style = new SearchingForStyle(this);
            searchingFor.add(style);
            return style;
        }
        private final List<SearchingForStyle> searchingFor = new ArrayList<>();

        @Override
        public void run(StepContext sc) {
            sc.english();
            sc.page.navigate(sc.baseUrl + "register");

            if (name != null) {
                sc.page.locator("#nameField").locator("input").fill(name);
            }
            if (email != null) {
                sc.page.locator("#emailField").locator("input").fill(email);
            }
            if (password != null) {
                sc.page.locator("#passwordField").locator("input").fill(password);
            }
            if (confirmPassword != null) {
                sc.page.locator("#confirmPasswordField").locator("input").fill(confirmPassword);
            }
            if (city != null) {
                selectComboBoxItem(sc, "#cityComboBox", city, true);
            }
            if (sex != null) {
                selectComboBoxItem(sc, "#sexComboBox", sex, true);
            }
            if (whoami != null) {
                sc.page.locator("#whoamiField").locator("textarea").fill(whoami);
            }
            if (whatdoiwant != null) {
                sc.page.locator("#whatdoiwantField").locator("textarea").fill(whatdoiwant);
            }
            if (weekFrequencyMin != null) {
                sc.page.locator("#weekFrequencyMinField").locator("input").fill("" + weekFrequencyMin);
            }
            if (weekFrequencyMax != null) {
                sc.page.locator("#weekFrequencyMaxField").locator("input").fill("" + weekFrequencyMax);
            }
            if (maxDistance != null) {
                sc.page.locator("#maxDistanceField").locator("input").fill("" + maxDistance);
            }
            if (active != null) {
                sc.page.locator("#activeCheckbox").locator("input").setChecked(active);
            }
            if (publiclyFindable != null) {
                sc.page.locator("#publiclyFindableCheckbox").locator("input").setChecked(publiclyFindable);
            }
            if (privacyAgreement != null) {
                sc.page.locator("#privacyAgreementCheckbox").locator("input").setChecked(privacyAgreement);
            }

            // "Dances I can do" rows
            for (int index = 0; index < canDo.size(); index++) {
                DancestyleStyle style = canDo.get(index);
                sc.page.locator("#addDancestyleButton").click();
                Locator row = sc.page.locator("#canDo" + index);
                if (style.dancestyle != null) {
                    selectComboBoxItem(sc, row.locator("#" + CandoRow.STYLE_COMBO_BOX_ID), style.dancestyle, true);
                }
                if (style.role != null) {
                    selectRoleItem(sc, row.locator("#" + CandoRow.ROLE_SELECT_ID), style.role);
                }
                if (style.skilllevel != null) {
                    selectComboBoxItem(sc, row.locator("#" + CandoRow.SKILLLEVEL_COMBO_BOX_ID), style.skilllevel + " - ", false);
                }
            }

            // "Searching for" rows
            for (int index = 0; index < searchingFor.size(); index++) {
                SearchingForStyle style = searchingFor.get(index);
                sc.page.locator("#addSearchingForButton").click();
                Locator row = sc.page.locator("#searchingFor" + index);
                if (style.dancestyle != null) {
                    selectComboBoxItem(sc, row.locator("#" + SearchingForRow.STYLE_COMBO_BOX_ID), style.dancestyle, true);
                }
                if (style.sex != null) {
                    selectComboBoxItem(sc, row.locator("#" + SearchingForRow.SEARCH_CRITERIA_SEX_COMBO_BOX_ID), style.sex, true);
                }
                if (style.role != null) {
                    selectRoleItem(sc, row.locator("#" + SearchingForRow.ROLE_SELECT_ID), style.role);
                }
                if (style.skilllevelMin != null) {
                    selectComboBoxItem(sc, row.locator("#" + SearchingForRow.SKILLLEVEL_MIN_COMBO_BOX_ID), style.skilllevelMin + " - ", false);
                }
                if (style.skilllevelMax != null) {
                    selectComboBoxItem(sc, row.locator("#" + SearchingForRow.SKILLLEVEL_MAX_COMBO_BOX_ID), style.skilllevelMax + " - ", false);
                }
            }
        }

        private static void selectComboBoxItem(StepContext sc, String selector, String text, boolean exact) {
            selectComboBoxItem(sc, sc.page.locator(selector), text, exact);
        }

        private static void selectComboBoxItem(StepContext sc, Locator comboBox, String text, boolean exact) {
            comboBox.locator("#toggleButton").click();
            comboBox.locator("vaadin-combo-box-item")
                    .getByText(text, new Locator.GetByTextOptions().setExact(exact))
                    .click();
        }

        private static void selectRoleItem(StepContext sc, Locator select, String text) {
            select.locator("vaadin-select-value-button").click();
            select.locator("vaadin-select-item")
                    .getByText(text, new Locator.GetByTextOptions().setExact(true))
                    .click();
        }
    }

    public static class DancestyleStyle implements When<StepContext> {
        private final Form form;
        private String dancestyle;
        private String role;
        private Integer skilllevel;

        DancestyleStyle(Form form) {
            this.form = form;
        }

        public DancestyleStyle dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public DancestyleStyle role(String v) {
            this.role = v;
            return this;
        }

        public DancestyleStyle skilllevel(int v) {
            this.skilllevel = v;
            return this;
        }

        public DancestyleStyle and() {
            return form.canDo();
        }

        public Form also() {
            return form;
        }

        @Override
        public void run(StepContext sc) {
            form.run(sc);
        }
    }

    public static class SearchingForStyle implements When<StepContext> {
        private final Form form;
        private String dancestyle;
        private String sex;
        private String role;
        private Integer skilllevelMin;
        private Integer skilllevelMax;

        SearchingForStyle(Form form) {
            this.form = form;
        }

        public SearchingForStyle dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public SearchingForStyle sex(String v) {
            this.sex = v;
            return this;
        }

        public SearchingForStyle role(String v) {
            this.role = v;
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

        public SearchingForStyle or() {
            return form.searchingFor();
        }

        public Form also() {
            return form;
        }

        @Override
        public void run(StepContext sc) {
            form.run(sc);
        }
    }
}
