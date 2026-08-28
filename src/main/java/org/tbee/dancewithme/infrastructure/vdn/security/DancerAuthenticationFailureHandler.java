package org.tbee.dancewithme.infrastructure.vdn.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.tbee.dancewithme.infrastructure.vdn.RememberedEmail;

import java.io.IOException;

/**
 * Reports a login attempt with a not yet confirmed email address for what it is.
 * {@link DancerUserDetailsService} throws a {@link DisabledException} in that case; without this handler that would
 * end up on the login page as a plain "wrong credentials" error, leaving the dancer with no way forward.
 * Instead the dancer is sent to the confirmation page, where the code can be entered or the confirmation email resent.
 */
public class DancerAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AuthenticationFailureHandler badCredentials = new SimpleUrlAuthenticationFailureHandler("/login?error");
    private final AuthenticationFailureHandler emailNotConfirmed = new SimpleUrlAuthenticationFailureHandler("/confirm?unconfirmed=true");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof DisabledException) {
            // so the confirmation page and the resend page can prefill it
            RememberedEmail.remember(request, request.getParameter("username"));
            emailNotConfirmed.onAuthenticationFailure(request, response, exception);
        }
        else {
            badCredentials.onAuthenticationFailure(request, response, exception);
        }
    }
}
