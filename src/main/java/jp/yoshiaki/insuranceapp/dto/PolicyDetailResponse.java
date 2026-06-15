package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 契約詳細画面の表示用DTO
 *
 * 【Day92で追加】
 * - canRenew / canUnrenew / canCancel / canUncancel の4フラグ
 * - status は effectiveStatus（契約中/解約/失効）を使用
 *
 * なぜDTOに「操作可否フラグ」を持たせるのか：
 *   - 画面（Thymeleaf）側で「このボタンを表示するか」を判定する必要がある
 *   - 判定ロジックをテンプレートに書くと複雑になるため、
 *     DTO変換時にService/Entity側で計算し、booleanフラグとして渡す
 *   - 画面テンプレートは「trueなら表示」とだけ書けばよい（責務分離）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDetailResponse {

    private Long id;
    private String policyNumber;
    private String customerName;
    private String startDate;
    private String endDate;
    private String contractPeriod;

    /** 更新可能期間と早期更改期限の表示情報 */
    private String renewableStartDate;
    private String earlyRenewalDeadline;
    private String today;
    private long daysUntilExpiry;
    private long daysFromEarlyRenewalDeadline;
    private double todayPositionPercent;
    private double earlyDeadlinePositionPercent;
    private boolean showTodayMarker;
    private String renewalTimingStatus;
    private String renewalTimingMessage;

    /** 表示用ステータス（契約中 / 解約 / 失効） */
    private String status;

    /** 要注意フラグ（満期20日前〜満期日） */
    private boolean attentionRequired;

    /** カレンダー登録済みフラグ */
    private boolean calendarRegistered;

    // ─── Day92で追加：操作可否フラグ ───

    /** 更新ボタンを表示するか（更新可能期間内 かつ ACTIVE） */
    private boolean canRenew;

    /** 更新取消ボタンを表示するか（更新済み かつ 当日） */
    private boolean canUnrenew;

    /** 解約ボタンを表示するか（effectiveStatus が 契約中） */
    private boolean canCancel;

    /** 解約取消ボタンを表示するか（解約済み かつ 当日） */
    private boolean canUncancel;

    /**
     * Entity → DTO 変換
     *
     * ここで各操作の可否を計算してフラグに設定する。
     * 画面テンプレートは「canRenew が true なら更新ボタンを表示」
     * とだけ書けばよく、判定ロジックを知る必要がない。
     *
     * @param policy 契約Entity
     * @return 画面表示用DTO
     */
    public static PolicyDetailResponse from(Policy policy) {
        return from(policy, LocalDate.now());
    }

    static PolicyDetailResponse from(Policy policy, LocalDate today) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate endDate = policy.getEndDate();
        String effectiveStatus = policy.getEffectiveStatus();
        LocalDate renewableStart = endDate.minusMonths(2);
        LocalDate earlyRenewalDeadline = endDate.minusDays(21);
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, endDate);
        long daysFromEarlyDeadline = ChronoUnit.DAYS.between(earlyRenewalDeadline, today);
        double todayPosition = calculateTimelinePosition(renewableStart, endDate, today);
        double earlyDeadlinePosition =
                calculateTimelinePosition(renewableStart, endDate, earlyRenewalDeadline);

        // 更新取消の可否：renewedAtが記録されていて、当日であり、旧満期日が退避されている
        boolean canUnrenew = policy.getRenewedAt() != null
                && policy.getRenewedAt().toLocalDate().equals(today)
                && policy.getRenewalDueEndDate() != null;

        // 解約取消の可否：cancelledAtが記録されていて、当日である
        boolean canUncancel = policy.getCancelledAt() != null
                && policy.getCancelledAt().toLocalDate().equals(today);

        return PolicyDetailResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(policy.getCustomerName())
                .startDate(policy.getStartDate().format(formatter))
                .endDate(endDate.format(formatter))
                .contractPeriod(policy.getStartDate().format(formatter)
                        + " ～ " + endDate.format(formatter))
                .renewableStartDate(renewableStart.format(formatter))
                .earlyRenewalDeadline(earlyRenewalDeadline.format(formatter))
                .today(today.format(formatter))
                .daysUntilExpiry(daysUntilExpiry)
                .daysFromEarlyRenewalDeadline(daysFromEarlyDeadline)
                .todayPositionPercent(todayPosition)
                .earlyDeadlinePositionPercent(earlyDeadlinePosition)
                .showTodayMarker("契約中".equals(effectiveStatus))
                .renewalTimingStatus(resolveRenewalTimingStatus(
                        effectiveStatus, today, renewableStart, earlyRenewalDeadline, endDate))
                .renewalTimingMessage(resolveRenewalTimingMessage(
                        effectiveStatus, today, renewableStart, earlyRenewalDeadline, endDate))
                // effectiveStatus を使用（DB値ではなく表示用の3値）
                .status(effectiveStatus)
                .attentionRequired(policy.isAttentionRequired())
                .calendarRegistered(policy.getCalendarRegistered())
                // 操作可否フラグ
                .canRenew(policy.isRenewable())
                .canUnrenew(canUnrenew)
                .canCancel("契約中".equals(policy.getEffectiveStatus()))
                .canUncancel(canUncancel)
                .build();
    }

    private static double calculateTimelinePosition(
            LocalDate periodStart, LocalDate periodEnd, LocalDate targetDate) {
        long totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd);
        if (totalDays <= 0) {
            return 0;
        }
        long elapsedDays = ChronoUnit.DAYS.between(periodStart, targetDate);
        double position = (double) elapsedDays / totalDays * 100;
        return Math.max(0, Math.min(100, position));
    }

    private static String resolveRenewalTimingStatus(
            String effectiveStatus,
            LocalDate today,
            LocalDate renewableStart,
            LocalDate earlyRenewalDeadline,
            LocalDate endDate) {
        if (!"契約中".equals(effectiveStatus)) {
            return "expired";
        }
        if (today.isAfter(endDate)) {
            return "expired";
        }
        if (today.isAfter(earlyRenewalDeadline)) {
            return "urgent";
        }
        if (!today.isBefore(renewableStart)) {
            return "early";
        }
        return "before";
    }

    private static String resolveRenewalTimingMessage(
            String effectiveStatus,
            LocalDate today,
            LocalDate renewableStart,
            LocalDate earlyRenewalDeadline,
            LocalDate endDate) {
        if ("解約".equals(effectiveStatus)) {
            return "本契約は解約済みのため、更新手続きの対象外です";
        }
        if ("失効".equals(effectiveStatus)) {
            return "本契約は満期を迎え、現在は失効しています";
        }
        if (today.isAfter(endDate)) {
            long elapsed = ChronoUnit.DAYS.between(endDate, today);
            return "満期日を" + elapsed + "日経過しています";
        }

        long daysUntilExpiry = ChronoUnit.DAYS.between(today, endDate);
        if (today.isAfter(earlyRenewalDeadline)) {
            long elapsed = ChronoUnit.DAYS.between(earlyRenewalDeadline, today);
            return "早期更改期限を" + elapsed + "日経過　満期まであと"
                    + daysUntilExpiry + "日です";
        }
        if (!today.isBefore(renewableStart)) {
            long remaining = ChronoUnit.DAYS.between(today, earlyRenewalDeadline);
            return "早期更改期限まであと" + remaining + "日　満期まであと"
                    + daysUntilExpiry + "日です";
        }

        long untilRenewable = ChronoUnit.DAYS.between(today, renewableStart);
        return "更新可能期間の開始まであと" + untilRenewable + "日です";
    }
}
