package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.tbee.dancewithme.giwth.Register;
import org.tbee.giwth.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterWebTest extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void populateAll() {
        Scenario.of(context(port))
                .when(Register.on()
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
                        .privacyAgreement(true))
        ;
        //sleepForALongTime();
    }
}
