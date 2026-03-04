package jp.yoshiaki.insuranceapp.service.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.repository.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 更新統計サービス
 * - 「当年度（4/1〜翌3/31）」の更新済み件数・早期/期限内/遅延を集計
 *
 * ※本番運用では、早期/遅延の定義（閾値）は業務ルールとして外出し（設定/マスタ化）推奨。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RenewalStatsService {

    private final PolicyRepository policyRepository;

    public FiscalYearStats calculateFiscalYearStats(LocalDate today) {
        LocalDate fiscalYearStart = fiscalYearStart(today);
        LocalDate fiscalYearEnd = fiscalYearEnd(today);

        log.debug("年度範囲: {} ～ {}", fiscalYearStart, fiscalYearEnd);

        List<Policy> renewedPolicies =
            policyRepository.findRenewedPoliciesInPeriod(fiscalYearStart, fiscalYearEnd);

        log.debug("当年度更新済み件数: {}", renewedPolicies.size());

        long early = renewedPolicies.stream().filter(this::isEarlyRenewal).count();
        long onTime = renewedPolicies.stream().filter(this::isOnTimeRenewal).count();
        long late = renewedPolicies.stream().filter(this::isLateRenewal).count();

        return new FiscalYearStats(renewedPolicies.size(), early, onTime, late);
    }

    /**
     * 早期更新：更新満了日の20日前より前に更新したもの
     */
    private boolean isEarlyRenewal(Policy p) {
        if (p.getRenewedAt() == null || p.getRenewalDueEndDate() == null) return false;
        LocalDate renewedDate = p.getRenewedAt().toLocalDate();
        return renewedDate.isBefore(p.getRenewalDueEndDate().minusDays(20));
    }

    /**
     * 期限内更新：満了日の20日前〜満了日（当日含む）に更新したもの
     */
    private boolean isOnTimeRenewal(Policy p) {
        if (p.getRenewedAt() == null || p.getRenewalDueEndDate() == null) return false;
        LocalDate renewedDate = p.getRenewedAt().toLocalDate();
        LocalDate due = p.getRenewalDueEndDate();
        return !renewedDate.isBefore(due.minusDays(20)) && !renewedDate.isAfter(due);
    }

    /**
     * 遅延更新：満了日を過ぎて更新したもの
     */
    private boolean isLateRenewal(Policy p) {
        if (p.getRenewedAt() == null || p.getRenewalDueEndDate() == null) return false;
        LocalDate renewedDate = p.getRenewedAt().toLocalDate();
        return renewedDate.isAfter(p.getRenewalDueEndDate());
    }

    private LocalDate fiscalYearStart(LocalDate today) {
        // 日本の年度：4/1開始
        int year = (today.getMonthValue() >= 4) ? today.getYear() : today.getYear() - 1;
        return LocalDate.of(year, 4, 1);
    }

    private LocalDate fiscalYearEnd(LocalDate today) {
        int year = (today.getMonthValue() >= 4) ? today.getYear() + 1 : today.getYear();
        return LocalDate.of(year, 3, 31);
    }

    public record FiscalYearStats(int renewedTotal, long earlyRenewal, long onTimeRenewal, long lateRenewal) {}
}
