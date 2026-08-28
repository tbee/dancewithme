package org.tbee.dancewithme.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DancerServiceRegisterTest {

    @Mock
    private DancerRepository dancerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailConfirmationService emailConfirmationService;

    private DancerService service() {
        return new DancerService(dancerRepository, passwordEncoder, emailConfirmationService);
    }

    @Test
    void registerWithNewEmailCreatesTheDancerAndRequestsConfirmation() {
        Dancer dancer = new Dancer().email("a@b.org");
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(dancerRepository.save(dancer)).thenReturn(dancer);

        DancerService.Registration registration = service().registerOrResend(dancer, "secret");

        assertThat(registration.resentForExisting()).isFalse();
        assertThat(registration.dancer()).isSameAs(dancer);
        assertThat(dancer.password()).isEqualTo("hashed");
        assertThat(dancer.privacyAgreementAcceptedAt()).isNotNull();
        verify(emailConfirmationService).requestConfirmation(dancer);
    }

    @Test
    void registerWithUnconfirmedEmailOnlyResendsForTheExistingDancer() {
        Dancer existing = new Dancer().email("a@b.org").name("Original");
        Dancer attempt = new Dancer().email("a@b.org").name("Attempt");
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(existing));

        DancerService.Registration registration = service().registerOrResend(attempt, "secret");

        assertThat(registration.resentForExisting()).isTrue();
        assertThat(registration.dancer()).isSameAs(existing);
        assertThat(existing.name()).isEqualTo("Original");
        verify(emailConfirmationService).requestConfirmation(existing);
        verify(dancerRepository, never()).save(any());
    }

    @Test
    void registerWithConfirmedEmailIsRefused() {
        Dancer existing = new Dancer().email("a@b.org").emailConfirmedAt(LocalDateTime.now());
        when(dancerRepository.findByEmail("a@b.org")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service().registerOrResend(new Dancer().email("a@b.org"), "secret"))
                .isInstanceOf(DancerService.EmailAlreadyRegisteredException.class);
        verify(dancerRepository, never()).save(any());
        verify(emailConfirmationService, never()).requestConfirmation(any());
    }
}
