package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.dancewithme.infrastructure.vdn.component.CandoRow;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchingForRow;
import org.tbee.giwth.Then;
import org.tbee.giwth.When;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Registration {

    public static Registers registers() {
        return new Registers();
    }

    public static class Registers extends Data<Registers> implements When<StepContext> {

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
                CanDo<Registers, ?> style = canDo.get(index);
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
                SearchingFor<Registers, ?> style = searchingFor.get(index);
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
        }

        @Override
        public RegistersCanDo canDo() {
            RegistersCanDo row = new RegistersCanDo(this);
            canDo.add(row);
            return row;
        }

        @Override
        public RegistersSearchingFor searchingFor() {
            RegistersSearchingFor row = new RegistersSearchingFor(this);
            searchingFor.add(row);
            return row;
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

    public static ShouldHaveSaved shouldHaveSaved() {
        return new ShouldHaveSaved();
    }

    public static class ShouldHaveSaved extends Data<ShouldHaveSaved> implements Then<StepContext> {

        @Override
        public void run(StepContext sc) {
            sc.inTransaction(() -> {
                Dancer dancer = sc.dancerRepository().findByEmail(email).orElseThrow();

                if (name != null) {
                    assertThat(dancer.name()).isEqualTo(name);
                }
                if (password != null) {
                    PasswordEncoder passwordEncoder = sc.beanFactory.getBean(PasswordEncoder.class);
                    assertThat(passwordEncoder.matches(password, dancer.password())).isTrue();
                }
                if (city != null) {
                    assertThat(dancer.city()).isNotNull();
                    assertThat(dancer.city().name()).isEqualTo(city);
                }
                if (sex != null) {
                    assertThat(dancer.sex()).isEqualTo(Sex.valueOf(sex.toUpperCase()));
                }
                if (whoami != null) {
                    assertThat(dancer.whoami()).isEqualTo(whoami);
                }
                if (whatdoiwant != null) {
                    assertThat(dancer.whatdoiwant()).isEqualTo(whatdoiwant);
                }
                if (weekFrequencyMin != null) {
                    assertThat(dancer.weekFrequencyMin()).isEqualTo(weekFrequencyMin);
                }
                if (weekFrequencyMax != null) {
                    assertThat(dancer.weekFrequencyMax()).isEqualTo(weekFrequencyMax);
                }
                if (maxDistance != null) {
                    assertThat(dancer.distanceMax()).isEqualTo(maxDistance);
                }
                if (active != null) {
                    assertThat(dancer.active()).isEqualTo(active);
                }
                if (publiclyFindable != null) {
                    assertThat(dancer.publiclyFindable()).isEqualTo(publiclyFindable);
                }

                assertThat(dancer.dancestyles()).hasSize(canDo.size());
                for (int i = 0; i < canDo.size(); i++) {
                    DancerDancestyle dd = dancer.dancestyles().get(i);
                    CanDo<ShouldHaveSaved, ?> expected = canDo.get(i);
                    assertThat(dd.dancestyle().name()).isEqualTo(expected.dancestyle);
                    assertThat(dd.role()).isEqualTo(Role.valueOf(expected.role.toUpperCase()));
                    assertThat(dd.skilllevel()).isEqualTo(expected.skilllevel);
                }

                assertThat(dancer.searchingFor()).hasSize(searchingFor.size());
                for (int i = 0; i < searchingFor.size(); i++) {
                    DancerSearchingFor sf = dancer.searchingFor().get(i);
                    SearchingFor<ShouldHaveSaved, ?> expected = searchingFor.get(i);
                    assertThat(sf.dancestyle().name()).isEqualTo(expected.dancestyle);
                    assertThat(sf.sex()).isEqualTo(SearchCriteriaSex.valueOf(expected.sex.toUpperCase()));
                    assertThat(sf.role()).isEqualTo(Role.valueOf(expected.role.toUpperCase()));
                    assertThat(sf.skilllevelMin()).isEqualTo(expected.skilllevelMin);
                    assertThat(sf.skilllevelMax()).isEqualTo(expected.skilllevelMax);
                }
            });
        }

        @Override
        public ShouldHaveSavedCanDo canDo() {
            ShouldHaveSavedCanDo row = new ShouldHaveSavedCanDo(this);
            canDo.add(row);
            return row;
        }

        @Override
        public ShouldHaveSavedSearchingFor searchingFor() {
            ShouldHaveSavedSearchingFor row = new ShouldHaveSavedSearchingFor(this);
            searchingFor.add(row);
            return row;
        }
    }

    // -----------------------------------

    public abstract static class Data<SELF extends Data<SELF>> {
        @SuppressWarnings("unchecked")
        protected SELF self() {
            return (SELF) this;
        }

        protected String name;
        protected String email;
        protected String password;
        protected String confirmPassword;
        protected String city;
        protected String sex;
        protected String whoami;
        protected String whatdoiwant;
        protected Integer weekFrequencyMin;
        protected Integer weekFrequencyMax;
        protected Integer maxDistance;
        protected Boolean active;
        protected Boolean publiclyFindable;
        protected Boolean privacyAgreement;

        public SELF name(String v) {
            this.name = v;
            return self();
        }

        public SELF email(String v) {
            this.email = v;
            return self();
        }

        public SELF password(String v) {
            this.password = v;
            return self();
        }

        public SELF confirmPassword(String v) {
            this.confirmPassword = v;
            return self();
        }

        public SELF city(String v) {
            this.city = v;
            return self();
        }

        public SELF sex(String v) {
            this.sex = v;
            return self();
        }

        public SELF whoami(String v) {
            this.whoami = v;
            return self();
        }

        public SELF whatdoiwant(String v) {
            this.whatdoiwant = v;
            return self();
        }

        public SELF weekFrequencyMin(int v) {
            this.weekFrequencyMin = v;
            return self();
        }

        public SELF weekFrequencyMax(int v) {
            this.weekFrequencyMax = v;
            return self();
        }

        public SELF maxDistance(int v) {
            this.maxDistance = v;
            return self();
        }

        public SELF active(boolean v) {
            this.active = v;
            return self();
        }

        public SELF publiclyFindable(boolean v) {
            this.publiclyFindable = v;
            return self();
        }

        public SELF privacyAgreement(boolean v) {
            this.privacyAgreement = v;
            return self();
        }

        public abstract CanDo<SELF, ?> canDo();
        public abstract SearchingFor<SELF, ?> searchingFor();

        protected final List<CanDo<SELF, ?>> canDo = new ArrayList<>();
        protected final List<SearchingFor<SELF, ?>> searchingFor = new ArrayList<>();
    }

    // -----------------------------------

    public static class CanDo<SELF extends Data<SELF>, ROW extends CanDo<SELF, ROW>> {
        protected final SELF data;
        protected String dancestyle;
        protected String role;
        protected Integer skilllevel;

        CanDo(SELF data) {
            this.data = data;
        }

        @SuppressWarnings("unchecked")
        protected ROW self() {
            return (ROW) this;
        }

        public ROW dancestyle(String v) {
            this.dancestyle = v;
            return self();
        }

        public ROW role(String v) {
            this.role = v;
            return self();
        }

        public ROW skilllevel(int v) {
            this.skilllevel = v;
            return self();
        }
    }

    public static class SearchingFor<SELF extends Data<SELF>, ROW extends SearchingFor<SELF, ROW>> {
        protected final SELF data;
        protected String dancestyle;
        protected String sex;
        protected String role;
        protected Integer skilllevelMin;
        protected Integer skilllevelMax;

        SearchingFor(SELF data) {
            this.data = data;
        }

        @SuppressWarnings("unchecked")
        protected ROW self() {
            return (ROW) this;
        }

        public ROW dancestyle(String v) {
            this.dancestyle = v;
            return self();
        }

        public ROW sex(String v) {
            this.sex = v;
            return self();
        }

        public ROW role(String v) {
            this.role = v;
            return self();
        }

        public ROW skilllevelMin(int v) {
            this.skilllevelMin = v;
            return self();
        }

        public ROW skilllevelMax(int v) {
            this.skilllevelMax = v;
            return self();
        }
    }

    // -----------------------------------

    public static class RegistersCanDo extends CanDo<Registers, RegistersCanDo> implements When<StepContext> {

        RegistersCanDo(Registers data) {
            super(data);
        }

        public RegistersCanDo and() {
            return data.canDo();
        }

        public Registers also() {
            return data;
        }

        @Override
        public void run(StepContext sc) {
            data.run(sc);
        }
    }

    public static class RegistersSearchingFor extends SearchingFor<Registers, RegistersSearchingFor> implements When<StepContext> {

        RegistersSearchingFor(Registers data) {
            super(data);
        }

        public RegistersSearchingFor or() {
            return data.searchingFor();
        }

        public Registers also() {
            return data;
        }

        @Override
        public void run(StepContext sc) {
            data.run(sc);
        }
    }

    public static class ShouldHaveSavedCanDo extends CanDo<ShouldHaveSaved, ShouldHaveSavedCanDo> implements Then<StepContext> {

        ShouldHaveSavedCanDo(ShouldHaveSaved data) {
            super(data);
        }

        public ShouldHaveSavedCanDo and() {
            return data.canDo();
        }

        public ShouldHaveSaved also() {
            return data;
        }

        @Override
        public void run(StepContext sc) {
            data.run(sc);
        }
    }

    public static class ShouldHaveSavedSearchingFor extends SearchingFor<ShouldHaveSaved, ShouldHaveSavedSearchingFor> implements Then<StepContext> {

        ShouldHaveSavedSearchingFor(ShouldHaveSaved data) {
            super(data);
        }

        public ShouldHaveSavedSearchingFor or() {
            return data.searchingFor();
        }

        public ShouldHaveSaved also() {
            return data;
        }

        @Override
        public void run(StepContext sc) {
            data.run(sc);
        }
    }
}