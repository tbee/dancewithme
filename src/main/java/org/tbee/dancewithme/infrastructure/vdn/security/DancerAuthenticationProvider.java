package org.tbee.dancewithme.infrastructure.vdn.security;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Authenticates dancers, and reports a not yet confirmed email address as an {@link EmailNotConfirmedException}
 * so {@link org.tbee.dancewithme.infrastructure.vdn.view.LoginView} can act on it.
 * <p>
 * A dancer who has not confirmed their email address is delivered as a disabled user by
 * {@link DancerUserDetailsService}. That check is deliberately moved from the pre-authentication checks (which run
 * before the password is verified, and would therefore tell anyone which email addresses are registered) to the
 * post-authentication checks: only someone who knows the password is told that the email needs confirming.
 * <p>
 * Throwing from {@link DancerUserDetailsService} itself is not an option: {@code DaoAuthenticationProvider} wraps
 * everything but a {@code UsernameNotFoundException} coming out of the user details service in an
 * {@code InternalAuthenticationServiceException}, which would arrive at the login page as a plain failure.
 */
public class DancerAuthenticationProvider extends DaoAuthenticationProvider {

    public DancerAuthenticationProvider(DancerUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super(userDetailsService);
        setPasswordEncoder(passwordEncoder);
        setPreAuthenticationChecks(preAuthenticationChecks());
        setPostAuthenticationChecks(postAuthenticationChecks());
    }

    /**
     * The default checks, minus the enabled check, which is postponed until after the password is verified.
     */
    private UserDetailsChecker preAuthenticationChecks() {
        return user -> {
            if (!user.isAccountNonLocked()) {
                throw new LockedException("Account is locked");
            }
            if (!user.isAccountNonExpired()) {
                throw new AccountExpiredException("Account has expired");
            }
        };
    }

    /**
     * The default checks, plus the postponed enabled check reported as a not confirmed email address.
     */
    private UserDetailsChecker postAuthenticationChecks() {
        return (UserDetails user) -> {
            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("Credentials have expired");
            }
            if (!user.isEnabled()) {
                throw new EmailNotConfirmedException(user.getUsername());
            }
        };
    }
}
