package org.tbee.dancewithme.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DancerCleanupServiceTest {

    @Mock
    private DancerRepository dancerRepository;

    @Test
    void purgeDeletesUnconfirmedDancersWithAnExpiredToken() {
        Dancer abandoned = new Dancer().email("a@b.org").emailConfirmationToken("123456")
                .emailConfirmationTokenExpiresAt(LocalDateTime.now().minusHours(1));
        when(dancerRepository.findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(any()))
                .thenReturn(List.of(abandoned));

        int purged = new DancerCleanupService(dancerRepository).purgeUnconfirmed();

        assertThat(purged).isEqualTo(1);
        verify(dancerRepository).deleteAll(List.of(abandoned));
    }

    @Test
    void purgeUsesTheCurrentMomentAsCutoff() {
        when(dancerRepository.findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(any()))
                .thenReturn(List.of());

        new DancerCleanupService(dancerRepository).purgeUnconfirmed();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dancerRepository).findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void purgeWithNothingToDoDeletesNothing() {
        when(dancerRepository.findByEmailConfirmedAtIsNullAndEmailConfirmationTokenExpiresAtBefore(any()))
                .thenReturn(List.of());

        assertThat(new DancerCleanupService(dancerRepository).purgeUnconfirmed()).isZero();
        verify(dancerRepository, never()).deleteAll(any());
    }
}
