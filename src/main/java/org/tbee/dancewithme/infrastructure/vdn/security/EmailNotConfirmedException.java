package org.tbee.dancewithme.infrastructure.vdn.security;

import org.springframework.security.authentication.DisabledException;

/**
 * Thrown when someone tries to log in with an email address that has not been confirmed yet.
 * Spring Security stores the failure in the session, so {@link org.tbee.dancewithme.infrastructure.vdn.view.LoginView}
 * can recognize this particular failure and send the dancer to the confirmation page instead of showing a
 * plain "wrong credentials" error, which would be a dead end.
 * It carries the email address, so the pages further along can prefill it.
 */
public class EmailNotConfirmedException extends DisabledException {

    private final String email;

    public EmailNotConfirmedException(String email) {
        super("Email not confirmed: " + email);
        this.email = email;
    }

    public String email() {
        return email;
    }
}
