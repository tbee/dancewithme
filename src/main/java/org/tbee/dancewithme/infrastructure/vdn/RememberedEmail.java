package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Remembers the email address a visitor last identified themselves with (registering, or a refused login),
 * so views further along in the flow can prefill it and the visitor does not have to type it again.
 * <p>
 * The value is kept in the HTTP session, because it is written both from Vaadin (views) and from plain servlet
 * code (the authentication failure handler), which has no {@code VaadinSession} available.
 */
public final class RememberedEmail {

    private static final String ATTRIBUTE = RememberedEmail.class.getName();

    private RememberedEmail() {
    }

    /**
     * Remembers the email address, from within a Vaadin view.
     */
    public static void remember(String email) {
        WrappedSession session = session();
        if (session != null) {
            session.setAttribute(ATTRIBUTE, email);
        }
    }

    /**
     * Remembers the email address, from within plain servlet code.
     */
    public static void remember(HttpServletRequest request, String email) {
        if (email != null && !email.isBlank()) {
            request.getSession().setAttribute(ATTRIBUTE, email);
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
