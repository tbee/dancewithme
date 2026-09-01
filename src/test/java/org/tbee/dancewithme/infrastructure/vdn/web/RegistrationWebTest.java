package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.tbee.dancewithme.giwth.Mail;
import org.tbee.dancewithme.giwth.Registration;
import org.tbee.giwth.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationWebTest extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void populateAll() {
        Scenario.of(context(port))
                .when(Registration.registers()
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .password("password123")
                        .confirmPassword("password123")
                        .city("Aalburg")
                        .sex("Male")
                        .whoami("I love to dance")
                        .whatdoiwant("Looking for a partner to dance with")
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(3)
                        .maxDistance(50)
                        .active(true)
                        .publiclyFindable(true)
                        .privacyAgreement(true)
                        .canDo()
                        .dancestyle("Ballroom").role("Lead").skilllevel(3)
                        .and()
                        .dancestyle("Latin").role("Follow").skilllevel(2)
                        .also()
                        .searchingFor()
                        .dancestyle("Ballroom").sex("Female").role("Follow").skilllevelMin(2).skilllevelMax(4)
                        .or()
                        .dancestyle("Latin").sex("Male").role("Lead").skilllevelMin(1).skilllevelMax(5))
                .then(Registration.shouldHaveSaved()
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .password("password123")
                        .confirmPassword("password123")
                        .city("Aalburg")
                        .sex("Male")
                        .whoami("I love to dance")
                        .whatdoiwant("Looking for a partner to dance with")
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(3)
                        .maxDistance(50)
                        .active(true)
                        .publiclyFindable(true)
                        .privacyAgreement(true)
                        .canDo()
                        .dancestyle("Ballroom").role("Lead").skilllevel(3)
                        .and()
                        .dancestyle("Latin").role("Follow").skilllevel(2)
                        .also()
                        .searchingFor()
                        .dancestyle("Ballroom").sex("Female").role("Follow").skilllevelMin(2).skilllevelMax(4)
                        .or()
                        .dancestyle("Latin").sex("Male").role("Lead").skilllevelMin(1).skilllevelMax(5))
                .and(Mail.shouldHaveBeenSent()
                        .to("john.doe@example.com")
                        .subject("Confirm your 'Shall we Dance?' account")
                        .textContaining("Your confirmation code is:"))
        ;
        //sleepForALongTime();
    }
}
