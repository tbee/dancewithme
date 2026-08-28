package org.tbee.dancewithme.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationServiceTest {

    @Mock
    private DancerRepository dancerRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private Environment environment;

    private EmailConfirmationService service() {
        return new EmailConfirmationService(dancerRepository, emailService, environment, "https://dancewithme.example.com");
    }

    @Test
    void requestConfirmationInDevelopmentGeneratesCodeWithoutEmail() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        Dancer dancer = new Dancer().email("a@b.org");

        String code = service().requestConfirmation(dancer);

        assertThat(code).matches("\\d{6}");
        assertThat(dancer.emailConfirmationToken()).isEqualTo(code);
        assertThat(dancer.emailConfirmedAt()).isNull();
        verify(dancerRepository).save(dancer);
        verify(emailService, never()).sendConfirmationEmail(any(), any(), any());
    }

    @Test
    void requestConfirmationInProductionSendsEmailWithCode() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        Dancer dancer = new Dancer().email("a@b.org");

        String code = service().requestConfirmation(dancer);

        assertThat(code).matches("\\d{6}");
        assertThat(dancer.emailConfirmedAt()).isNull();
        verify(dancerRepository).save(dancer);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendConfirmationEmail(eq("a@b.org"), eq(code), urlCaptor.capture());
        assertThat(urlCaptor.getValue()).isEqualTo("https://dancewithme.example.com/confirm?code=" + code);
    }

    @Test
    void confirmWithKnownCodeConfirmsAndClearsCode() {
        Dancer dancer = new Dancer().email("a@b.org").emailConfirmationToken("123456")
                .emailConfirmationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        when(dancerRepository.findByEmailConfirmationToken("123456")).thenReturn(Optional.of(dancer));

        boolean confirmed = service().confirm("123456");

        assertThat(confirmed).isTrue();
        assertThat(dancer.emailConfirmedAt()).isNotNull();
        assertThat(dancer.emailConfirmationToken()).isNull();
        assertThat(dancer.emailConfirmationTokenExpiresAt()).isNull();
        verify(dancerRepository).save(dancer);
    }

    @Test
    void confirmWithExpiredCodeReturnsFalse() {
        Dancer dancer = new Dancer().email("a@b.org").emailConfirmationToken("123456")
                .emailConfirmationTokenExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(dancerRepository.findByEmailConfirmationToken("123456")).thenReturn(Optional.of(dancer));

        boolean confirmed = service().confirm("123456");

        assertThat(confirmed).isFalse();
        assertThat(dancer.emailConfirmedAt()).isNull();
        verify(dancerRepository, never()).save(any());
    }

    @Test
    void requestConfirmationSetsExpiry() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        Dancer dancer = new Dancer().email("a@b.org");

        service().requestConfirmation(dancer);

        assertThat(dancer.emailConfirmationTokenExpiresAt())
                .isCloseTo(LocalDateTime.now().plusHours(EmailConfirmationService.TOKEN_EXPIRY_HOURS),
                        within(1, ChronoUnit.MINUTES));
    }

    @Test
    void resendConfirmationGeneratesNewCodeAndRestartsTheClock() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        Dancer dancer = new Dancer().email("a@b.org").emailConfirmationToken("111111")
                .emailConfirmationTokenExpiresAt(LocalDateTime.now().minusHours(5));
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(dancer));

        String code = service().resendConfirmation("a@b.org");

        assertThat(code).matches("\\d{6}").isNotEqualTo("111111");
        assertThat(dancer.emailConfirmationToken()).isEqualTo(code);
        assertThat(dancer.emailConfirmationTokenExpiresAt()).isAfter(LocalDateTime.now());
        verify(dancerRepository).save(dancer);
    }

    @Test
    void resendConfirmationForUnknownEmailDoesNothing() {
        when(dancerRepository.findByEmail("nobody@b.org")).thenReturn(Optional.empty());

        assertThat(service().resendConfirmation("nobody@b.org")).isNull();
        verify(dancerRepository, never()).save(any());
    }

    @Test
    void resendConfirmationForAlreadyConfirmedEmailDoesNothing() {
        Dancer dancer = new Dancer().email("a@b.org").emailConfirmedAt(LocalDateTime.now());
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(dancer));

        assertThat(service().resendConfirmation("a@b.org")).isNull();
        verify(dancerRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationEmail(any(), any(), any());
    }

    @Test
    void confirmWithUnknownCodeReturnsFalse() {
        when(dancerRepository.findByEmailConfirmationToken("000000")).thenReturn(Optional.empty());

        boolean confirmed = service().confirm("000000");

        assertThat(confirmed).isFalse();
        verify(dancerRepository, never()).save(any());
    }
}
