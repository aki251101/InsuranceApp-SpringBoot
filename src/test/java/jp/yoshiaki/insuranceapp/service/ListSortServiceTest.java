package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListSortServiceTest {

    private final ListSortService listSortService = new ListSortService();

    @Test
    void accidentDefaultSortUsesOccurredAtThenPolicyNumber() {
        Accident later = accident(1L, "P-2026-0001", LocalDate.of(2026, 6, 2), "OPEN");
        Accident sameDateLaterNumber =
                accident(2L, "P-2026-0002", LocalDate.of(2026, 6, 1), "OPEN");
        Accident sameDateEarlierNumber =
                accident(3L, "P-2026-0001", LocalDate.of(2026, 6, 1), "OPEN");

        List<Accident> sorted = listSortService.sortAccidents(
                List.of(later, sameDateLaterNumber, sameDateEarlierNumber),
                "occurredAt", "asc");

        assertThat(sorted).extracting(Accident::getId).containsExactly(3L, 2L, 1L);
    }

    @Test
    void accidentStatusSortUsesOccurredAtAsSecondaryKey() {
        Accident newerInProgress =
                accident(1L, "P-2026-0001", LocalDate.of(2026, 6, 2), "IN_PROGRESS");
        Accident open = accident(2L, "P-2026-0002", LocalDate.of(2026, 6, 3), "OPEN");
        Accident olderInProgress =
                accident(3L, "P-2026-0003", LocalDate.of(2026, 6, 1), "IN_PROGRESS");

        List<Accident> sorted = listSortService.sortAccidents(
                List.of(newerInProgress, open, olderInProgress), "status", "asc");

        assertThat(sorted).extracting(Accident::getId).containsExactly(2L, 3L, 1L);
    }

    @Test
    void policyDefaultSortUsesEndDateThenPolicyNumber() {
        Policy later = policy(1L, "P-2026-0001", LocalDate.of(2027, 6, 2));
        Policy sameDateLaterNumber =
                policy(2L, "P-2026-0002", LocalDate.of(2027, 6, 1));
        Policy sameDateEarlierNumber =
                policy(3L, "P-2026-0001", LocalDate.of(2027, 6, 1));

        List<Policy> sorted = listSortService.sortPolicies(
                List.of(later, sameDateLaterNumber, sameDateEarlierNumber),
                "endDate", "asc");

        assertThat(sorted).extracting(Policy::getId).containsExactly(3L, 2L, 1L);
    }

    @Test
    void policyNumberSortUsesEndDateAsSecondaryKey() {
        Policy laterSameNumber =
                policy(1L, "P-2026-0001", LocalDate.of(2027, 6, 2));
        Policy anotherNumber =
                policy(2L, "P-2026-0002", LocalDate.of(2027, 6, 1));
        Policy earlierSameNumber =
                policy(3L, "P-2026-0001", LocalDate.of(2027, 6, 1));

        List<Policy> sorted = listSortService.sortPolicies(
                List.of(laterSameNumber, anotherNumber, earlierSameNumber),
                "policyNumber", "asc");

        assertThat(sorted).extracting(Policy::getId).containsExactly(3L, 1L, 2L);
    }

    private Accident accident(
            Long id, String policyNumber, LocalDate occurredAt, String status) {
        Policy policy = Policy.builder()
                .id(id)
                .policyNumber(policyNumber)
                .customerName("契約者" + id)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .status("ACTIVE")
                .calendarRegistered(false)
                .build();

        return Accident.builder()
                .id(id)
                .policyId(id)
                .policy(policy)
                .occurredAt(occurredAt)
                .status(status)
                .memo("")
                .build();
    }

    private Policy policy(Long id, String policyNumber, LocalDate endDate) {
        return Policy.builder()
                .id(id)
                .policyNumber(policyNumber)
                .customerName("契約者" + id)
                .startDate(endDate.minusYears(1))
                .endDate(endDate)
                .status("ACTIVE")
                .calendarRegistered(false)
                .build();
    }
}
