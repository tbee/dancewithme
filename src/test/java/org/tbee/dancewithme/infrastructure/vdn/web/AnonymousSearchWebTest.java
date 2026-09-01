package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.tbee.dancewithme.giwth.Populate;
import org.tbee.dancewithme.giwth.Search;
import org.tbee.dancewithme.giwth.SearchResult;
import org.tbee.giwth.Scenario;

import static org.tbee.dancewithme.giwth.Populate.BALLROOM_FOLLOW_FEMALE_BEGINNER;
import static org.tbee.dancewithme.giwth.Populate.BALLROOM_LEAD_MALE_BEGINNER;
import static org.tbee.dancewithme.giwth.Populate.EMPTY_DANCER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnonymousSearchWebTest extends WebTestBase {

    @LocalServerPort
    private int port;

    @Test
    public void defaultSearch() {
        Scenario.of(context(port))
                .given(Populate.standardSetExists())
                .when(Search.on())
                .then(SearchResult.shouldContain(EMPTY_DANCER))
                .then(SearchResult.shouldContain(BALLROOM_LEAD_MALE_BEGINNER))
                .then(SearchResult.shouldContain(BALLROOM_FOLLOW_FEMALE_BEGINNER))
        ;
        //sleepForALongTime();
    }
}
