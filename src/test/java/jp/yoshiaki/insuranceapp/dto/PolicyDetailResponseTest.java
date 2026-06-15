package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Policy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDetailResponseTest {

    @Test
    void buildsRenewalTimelineForDateAfterEarlyRenewalDeadline() {
        Policy policy = Policy.builder()
                .id(10L)
                .policyNumber("P-2026-0107")
                .customerName("木村 大輔")
                .startDate(LocalDate.of(2024, 6, 22))
                .endDate(LocalDate.of(2026, 6, 22))
                .status("ACTIVE")
                .calendarRegistered(false)
                .build();

        PolicyDetailResponse response =
                PolicyDetailResponse.from(policy, LocalDate.of(2026, 6, 15));

        assertThat(response.getRenewableStartDate()).isEqualTo("2026/04/22");
        assertThat(response.getEarlyRenewalDeadline()).isEqualTo("2026/06/01");
        assertThat(response.getToday()).isEqualTo("2026/06/15");
        assertThat(response.getDaysUntilExpiry()).isEqualTo(7);
        assertThat(response.getDaysFromEarlyRenewalDeadline()).isEqualTo(14);
        assertThat(response.isShowTodayMarker()).isTrue();
        assertThat(response.getRenewalTimingStatus()).isEqualTo("urgent");
        assertThat(response.getRenewalTimingMessage())
                .isEqualTo("早期更改期限を14日経過　満期まであと7日です");
        assertThat(response.getEarlyDeadlinePositionPercent()).isBetween(0.0, 100.0);
        assertThat(response.getTodayPositionPercent())
                .isGreaterThan(response.getEarlyDeadlinePositionPercent())
                .isLessThan(100.0);
    }

    @Test
    void hidesTodayMarkerForCancelledPolicy() {
        Policy policy = Policy.builder()
                .id(11L)
                .policyNumber("P-2026-0108")
                .customerName("斎藤 直樹")
                .startDate(LocalDate.of(2025, 8, 12))
                .endDate(LocalDate.of(2026, 8, 12))
                .status("CANCELLED")
                .calendarRegistered(false)
                .build();

        PolicyDetailResponse response =
                PolicyDetailResponse.from(policy, LocalDate.of(2026, 6, 15));

        assertThat(response.getStatus()).isEqualTo("解約");
        assertThat(response.isShowTodayMarker()).isFalse();
        assertThat(response.getRenewalTimingStatus()).isEqualTo("expired");
        assertThat(response.getRenewalTimingMessage())
                .isEqualTo("本契約は解約済みのため、更新手続きの対象外です");
    }

    @Test
    void hidesTodayMarkerForExpiredPolicy() {
        Policy policy = Policy.builder()
                .id(2L)
                .policyNumber("P-2026-0002")
                .customerName("佐藤 花子")
                .startDate(LocalDate.of(2024, 5, 31))
                .endDate(LocalDate.of(2026, 5, 31))
                .status("ACTIVE")
                .calendarRegistered(false)
                .build();

        PolicyDetailResponse response =
                PolicyDetailResponse.from(policy, LocalDate.of(2026, 6, 15));

        assertThat(response.getStatus()).isEqualTo("失効");
        assertThat(response.isShowTodayMarker()).isFalse();
        assertThat(response.getRenewalTimingStatus()).isEqualTo("expired");
        assertThat(response.getRenewalTimingMessage())
                .isEqualTo("本契約は満期を迎え、現在は失効しています");
    }
}
