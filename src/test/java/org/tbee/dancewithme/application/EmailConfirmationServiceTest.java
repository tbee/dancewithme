package org.tbee.dancewithme.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        Dancer dancer = new Dancer().email("a@b.org").emailConfirmationToken("123456");
        when(dancerRepository.findByEmailConfirmationToken("123456")).thenReturn(Optional.of(dancer));

        boolean confirmed = service().confirm("123456");

        assertThat(confirmed).isTrue();
        assertThat(dancer.emailConfirmedAt()).isNotNull();
        assertThat(dancer.emailConfirmationToken()).isNull();
        verify(dancerRepository).save(dancer);
    }

    @Test
    void confirmWithUnknownCodeReturnsFalse() {
        when(dancerRepository.findByEmailConfirmationToken("000000")).thenReturn(Optional.empty());

        boolean confirmed = service().confirm("000000");

        assertThat(confirmed).isFalse();
        verify(dancerRepository, never()).save(any());
    }
}
