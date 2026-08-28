package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.WrappedSession;

/**
 * Remembers the email address a visitor last identified themselves with (registering, or a refused login),
 * so views further along in the flow can prefill it and the visitor does not have to type it again.
 * <p>
 * The value is kept in the HTTP session rather than on the Vaadin session, next to the authentication failure
 * that Spring Security parks there.
 */
public final class RememberedEmail {

    private static final String ATTRIBUTE = RememberedEmail.class.getName();

    private RememberedEmail() {
    }

    /**
     * Remembers the email address.
     */
    public static void remember(String email) {
        WrappedSession session = session();
        if (session != null && email != null && !email.isBlank()) {
            session.setAttribute(ATTRIBUTE, email);
        }
    }

    /**
     * @return the last remembered email address, or an empty string when there is none
     */
    public static String recall() {
        WrappedSession session = session();
        Object email = session == null ? null : session.getAttribute(ATTRIBUTE);
        return email == null ? "" : email.toString();
    }

    private static WrappedSession session() {
        return VaadinService.getCurrentRequest() == null ? null : VaadinService.getCurrentRequest().getWrappedSession();
    }
}
