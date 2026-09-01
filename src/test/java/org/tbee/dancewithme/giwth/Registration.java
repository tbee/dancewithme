package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import org.tbee.dancewithme.infrastructure.vdn.component.CandoRow;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchingForRow;
import org.tbee.giwth.When;

import java.util.ArrayList;
import java.util.List;

public class Registration {

    public static Registers registers() {
        return new Registers();
    }

    public static class Registers implements When<StepContext> {

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

        public Registers name(String v) {
            this.name = v;
            return this;
        }

        public Registers email(String v) {
            this.email = v;
            return this;
        }

        public Registers password(String v) {
            this.password = v;
            return this;
        }

        public Registers confirmPassword(String v) {
            this.confirmPassword = v;
            return this;
        }

        public Registers city(String v) {
            this.city = v;
            return this;
        }

        public Registers sex(String v) {
            this.sex = v;
            return this;
        }

        public Registers whoami(String v) {
            this.whoami = v;
            return this;
        }

        public Registers whatdoiwant(String v) {
            this.whatdoiwant = v;
            return this;
        }

        public Registers weekFrequencyMin(int v) {
            this.weekFrequencyMin = v;
            return this;
        }

        public Registers weekFrequencyMax(int v) {
            this.weekFrequencyMax = v;
            return this;
        }

        public Registers maxDistance(int v) {
            this.maxDistance = v;
            return this;
        }

        public Registers active(boolean v) {
            this.active = v;
            return this;
        }

        public Registers publiclyFindable(boolean v) {
            this.publiclyFindable = v;
            return this;
        }

        public Registers privacyAgreement(boolean v) {
            this.privacyAgreement = v;
            return this;
        }

        public RegistersCanDo canDo() {
            RegistersCanDo row = new RegistersCanDo(this);
            canDo.add(row);
            return row;
        }
        private final List<RegistersCanDo> canDo = new ArrayList<>();

        public RegistersSearchingFor searchingFor() {
            RegistersSearchingFor row = new RegistersSearchingFor(this);
            searchingFor.add(row);
            return row;
        }
        private final List<RegistersSearchingFor> searchingFor = new ArrayList<>();

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
                RegistersCanDo style = canDo.get(index);
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
                RegistersSearchingFor style = searchingFor.get(index);
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

            sc.page.locator("#registerButton").click();
            sc.waitForUrlToContain("/confirm");
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

    // -----------------------------------

    public static class RegistersCanDo implements When<StepContext> {

        private final Registers registers;
        private String dancestyle;
        private String role;
        private Integer skilllevel;

        RegistersCanDo(Registers registers) {
            this.registers = registers;
        }

        public RegistersCanDo dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public RegistersCanDo role(String v) {
            this.role = v;
            return this;
        }

        public RegistersCanDo skilllevel(int v) {
            this.skilllevel = v;
            return this;
        }

        public RegistersCanDo and() {
            return registers.canDo();
        }

        public Registers also() {
            return registers;
        }

        @Override
        public void run(StepContext sc) {
            registers.run(sc);
        }
    }

    // -----------------------------------

    public static class RegistersSearchingFor implements When<StepContext> {

        private final Registers registers;
        private String dancestyle;
        private String sex;
        private String role;
        private Integer skilllevelMin;
        private Integer skilllevelMax;

        RegistersSearchingFor(Registers registers) {
            this.registers = registers;
        }

        public RegistersSearchingFor dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public RegistersSearchingFor sex(String v) {
            this.sex = v;
            return this;
        }

        public RegistersSearchingFor role(String v) {
            this.role = v;
            return this;
        }

        public RegistersSearchingFor skilllevelMin(int v) {
            this.skilllevelMin = v;
            return this;
        }

        public RegistersSearchingFor skilllevelMax(int v) {
            this.skilllevelMax = v;
            return this;
        }

        public RegistersSearchingFor or() {
            return registers.searchingFor();
        }

        public Registers also() {
            return registers;
        }

        @Override
        public void run(StepContext sc) {
            registers.run(sc);
        }
    }
}