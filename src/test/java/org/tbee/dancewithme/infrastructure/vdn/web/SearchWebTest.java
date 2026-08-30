package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.tbee.dancewithme.giwth.Dancer;
import org.tbee.dancewithme.giwth.Populate;
import org.tbee.giwth.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchWebTest extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void simpleSearch() {
        Scenario.of(context(port))
                .given( Populate.standardSetExists() )
                .and(Dancer.isLoggedIn(Populate.DANCER1EMAIL) )
//
//                .when( Mail.openProvidersView() )
//                .and( Mail.pressMailButtonForProvider("provider1") )
//
//                .then( Mail.mailShouldBeSent()
//                        .to("provider1@example.com")
//                        .subjectContaining("rooster") )
        ;
        //sleepForALongTime();
    }
}
