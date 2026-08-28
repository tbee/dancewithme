package org.tbee.dancewithme.infrastructure.vdn.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DancerAuthenticationProviderTest {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Mock
    private DancerRepository dancerRepository;

    private DancerAuthenticationProvider provider() {
        return new DancerAuthenticationProvider(new DancerUserDetailsService(dancerRepository), PASSWORD_ENCODER);
    }

    private Dancer dancer() {
        return new Dancer().email("a@b.org").password(PASSWORD_ENCODER.encode("secret"));
    }

    @Test
    void confirmedDancerWithCorrectPasswordIsAuthenticated() {
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(dancer().emailConfirmedAt(LocalDateTime.now())));

        Authentication authentication = provider().authenticate(new UsernamePasswordAuthenticationToken("a@b.org", "secret"));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities()).extracting(Object::toString).contains("ROLE_USER");
    }

    @Test
    void unconfirmedDancerWithCorrectPasswordIsReportedAsNotConfirmed() {
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(dancer()));

        assertThatThrownBy(() -> provider().authenticate(new UsernamePasswordAuthenticationToken("a@b.org", "secret")))
                .isInstanceOf(EmailNotConfirmedException.class)
                .extracting(e -> ((EmailNotConfirmedException) e).email()).isEqualTo("a@b.org");
    }

    /**
     * Reporting the unconfirmed email before the password is verified would tell anyone which email addresses
     * are registered.
     */
    @Test
    void unconfirmedDancerWithWrongPasswordIsJustBadCredentials() {
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(dancer()));

        assertThatThrownBy(() -> provider().authenticate(new UsernamePasswordAuthenticationToken("a@b.org", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void unknownEmailIsJustBadCredentials() {
        when(dancerRepository.findByEmail("nobody@b.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider().authenticate(new UsernamePasswordAuthenticationToken("nobody@b.org", "secret")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
