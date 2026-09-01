package org.tbee.dancewithme.giwth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.giwth.Given;
import org.tbee.giwth.Then;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Dancer {

    public static IsLoggedIn isLoggedIn(String email) {
        return new IsLoggedIn(email);
    }

    public static Exists shouldExist() {
        return new Exists();
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

    public static class Exists implements Then<StepContext> {

        private String name;
        private String email;
        private String password;
        private String city;
        private String sex;
        private String whoami;
        private String whatdoiwant;
        private Integer weekFrequencyMin;
        private Integer weekFrequencyMax;
        private Integer distanceMax;
        private Boolean active;
        private Boolean publiclyFindable;

        public Exists name(String v) {
            this.name = v;
            return this;
        }

        public Exists email(String v) {
            this.email = v;
            return this;
        }

        public Exists password(String v) {
            this.password = v;
            return this;
        }

        public Exists city(String v) {
            this.city = v;
            return this;
        }

        public Exists sex(String v) {
            this.sex = v;
            return this;
        }

        public Exists whoami(String v) {
            this.whoami = v;
            return this;
        }

        public Exists whatdoiwant(String v) {
            this.whatdoiwant = v;
            return this;
        }

        public Exists weekFrequencyMin(int v) {
            this.weekFrequencyMin = v;
            return this;
        }

        public Exists weekFrequencyMax(int v) {
            this.weekFrequencyMax = v;
            return this;
        }

        public Exists distanceMax(int v) {
            this.distanceMax = v;
            return this;
        }

        public Exists active(boolean v) {
            this.active = v;
            return this;
        }

        public Exists publiclyFindable(boolean v) {
            this.publiclyFindable = v;
            return this;
        }

        public ExistsCanDo canDo() {
            ExistsCanDo row = new ExistsCanDo(this);
            canDo.add(row);
            return row;
        }

        public ExistsSearchingFor searchingFor() {
            ExistsSearchingFor row = new ExistsSearchingFor(this);
            searchingFor.add(row);
            return row;
        }

        private final List<ExistsCanDo> canDo = new ArrayList<>();
        private final List<ExistsSearchingFor> searchingFor = new ArrayList<>();

        @Override
        public void run(StepContext sc) {
            sc.inTransaction(() -> {
                org.tbee.dancewithme.domain.Dancer dancer = sc.dancerRepository().findByEmail(email).orElseThrow();

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
                if (distanceMax != null) {
                    assertThat(dancer.distanceMax()).isEqualTo(distanceMax);
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
                    ExistsCanDo expected = canDo.get(i);
                    assertThat(dd.dancestyle().name()).isEqualTo(expected.dancestyle);
                    assertThat(dd.role()).isEqualTo(Role.valueOf(expected.role.toUpperCase()));
                    assertThat(dd.skilllevel()).isEqualTo(expected.skilllevel);
                }

                assertThat(dancer.searchingFor()).hasSize(searchingFor.size());
                for (int i = 0; i < searchingFor.size(); i++) {
                    DancerSearchingFor sf = dancer.searchingFor().get(i);
                    ExistsSearchingFor expected = searchingFor.get(i);
                    assertThat(sf.dancestyle().name()).isEqualTo(expected.dancestyle);
                    assertThat(sf.sex()).isEqualTo(SearchCriteriaSex.valueOf(expected.sex.toUpperCase()));
                    assertThat(sf.role()).isEqualTo(Role.valueOf(expected.role.toUpperCase()));
                    assertThat(sf.skilllevelMin()).isEqualTo(expected.skilllevelMin);
                    assertThat(sf.skilllevelMax()).isEqualTo(expected.skilllevelMax);
                }
            });
        }
    }

    public static class ExistsCanDo implements Then<StepContext> {

        private final Exists exists;
        private String dancestyle;
        private String role;
        private Integer skilllevel;

        ExistsCanDo(Exists exists) {
            this.exists = exists;
        }

        public ExistsCanDo dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public ExistsCanDo role(String v) {
            this.role = v;
            return this;
        }

        public ExistsCanDo skilllevel(int v) {
            this.skilllevel = v;
            return this;
        }

        public ExistsCanDo and() {
            return exists.canDo();
        }

        public Exists also() {
            return exists;
        }

        @Override
        public void run(StepContext sc) {
            exists.run(sc);
        }
    }

    public static class ExistsSearchingFor implements Then<StepContext> {

        private final Exists exists;
        private String dancestyle;
        private String sex;
        private String role;
        private Integer skilllevelMin;
        private Integer skilllevelMax;

        ExistsSearchingFor(Exists exists) {
            this.exists = exists;
        }

        public ExistsSearchingFor dancestyle(String v) {
            this.dancestyle = v;
            return this;
        }

        public ExistsSearchingFor sex(String v) {
            this.sex = v;
            return this;
        }

        public ExistsSearchingFor role(String v) {
            this.role = v;
            return this;
        }

        public ExistsSearchingFor skilllevelMin(int v) {
            this.skilllevelMin = v;
            return this;
        }

        public ExistsSearchingFor skilllevelMax(int v) {
            this.skilllevelMax = v;
            return this;
        }

        public ExistsSearchingFor or() {
            return exists.searchingFor();
        }

        public Exists also() {
            return exists;
        }

        @Override
        public void run(StepContext sc) {
            exists.run(sc);
        }
    }
}