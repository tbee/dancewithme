package org.tbee.dancewithme;

/**
 * Starts the application against a locally running postgres database
 * (jdbc:postgresql://localhost:5418/dancewithme, see application-dev-postgres.properties).
 */
public class DancewithmePostgres {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "default,dev,dev-postgres"); // application-default.properties is in the project root folder, not checked into git.
        Dancewithme.main(args);
    }
}
