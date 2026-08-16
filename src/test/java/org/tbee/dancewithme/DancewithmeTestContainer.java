package org.tbee.dancewithme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgres.PostgreSQLOnHsqldbCompatibilityDialect;
import org.tbee.webstack.postgres.PostgresTestContainer;

public class DancewithmeTestContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DancewithmeTestContainer.class);
    private static final Logger LOGGER_TC = LoggerFactory.getLogger(PostgresTestContainer.class);

    public static void main(String[] args) throws Exception {
        // Using Rancher Desktop with moby on windows
        // Make sure DOCKER_HOST env variable is set (to npipe:////./pipe/docker_engine)
        System.out.println("DOCKER_HOST=" + System.getenv("DOCKER_HOST"));
        // Force docker-java to use API 1.41 instead of defaulting to 1.32
        System.setProperty("api.version", "1.41");

        // Make sure TESTCONTAINERS_DOCKER_HOST env variable is set
        // Make sure TESTCONTAINERS_RYUK_DISABLED=true is set in the run profile
        new PostgresTestContainer()
                .database("dancewithme")
                .username("dancewithme")
                .password("dancewithme")
                .log(LOGGER_TC::info)
                .start();
        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());
        System.setProperty("spring.jpa.properties.hibernate.dialect", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());

        System.setProperty("spring.profiles.active", "default,dev,dev-testcontainer"); // application-default.properties is in the project root folder, not checked into git.
        Dancewithme.main(args);
    }
}
