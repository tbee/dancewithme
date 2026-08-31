package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.tbee.dancewithme.giwth.Populate;
import org.tbee.dancewithme.giwth.Search;
import org.tbee.giwth.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnonymousSearchWebTest extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void defaultSearch() {
        Scenario.of(context(port))
                .given( Populate.standardSetExists() )

                .when( Search.on() )
        ;
        sleepForALongTime();
    }
}
