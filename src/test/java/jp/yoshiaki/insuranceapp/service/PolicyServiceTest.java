package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.repository.PolicyRepository;
import jp.yoshiaki.insuranceapp.util.PolicyNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CalendarService calendarService;

    @Mock
    private PolicyNumberGenerator policyNumberGenerator;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void rejectsCalendarRegistrationForCancelledPolicy() {
        Policy policy = policy("CANCELLED", LocalDate.now().plusMonths(3));
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> policyService.toggleCalendarRegistration(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("解約した契約はカレンダー登録できません");

        verifyNoInteractions(calendarService);
    }

    @Test
    void rejectsCalendarRegistrationForLapsedPolicy() {
        Policy policy = policy("ACTIVE", LocalDate.now().minusDays(1));
        when(policyRepository.findById(2L)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> policyService.toggleCalendarRegistration(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("失効した契約はカレンダー登録できません");

        verifyNoInteractions(calendarService);
    }

    @Test
    void rejectsAiSummaryForCancelledAndLapsedPolicies() {
        assertThatThrownBy(() -> policyService.validateAiSummaryAvailable(
                policy("CANCELLED", LocalDate.now().plusMonths(3))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("解約した契約はAI要約を表示できません");

        assertThatThrownBy(() -> policyService.validateAiSummaryAvailable(
                policy("ACTIVE", LocalDate.now().minusDays(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("失効した契約はAI要約を表示できません");
    }

    private Policy policy(String status, LocalDate endDate) {
        return Policy.builder()
                .id(1L)
                .policyNumber("P-2026-0001")
                .customerName("契約者")
                .startDate(endDate.minusYears(1))
                .endDate(endDate)
                .status(status)
                .calendarRegistered(false)
                .build();
    }
}
