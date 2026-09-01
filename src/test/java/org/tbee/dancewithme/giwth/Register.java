package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import org.tbee.giwth.When;

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

        @Override
        public void run(StepContext sc) {
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
                selectComboBoxItem(sc, "#cityComboBox", city);
            }
            if (sex != null) {
                selectComboBoxItem(sc, "#sexComboBox", sex);
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
        }

        private static void selectComboBoxItem(StepContext sc, String selector, String text) {
            Locator comboBox = sc.page.locator(selector);
            comboBox.locator("#toggleButton").click();
            comboBox.locator("vaadin-combo-box-item")
                    .getByText(text, new Locator.GetByTextOptions().setExact(true))
                    .click();
        }
    }
}
