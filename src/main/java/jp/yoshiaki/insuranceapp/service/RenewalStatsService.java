package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.dto.RenewalStatsDto;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 早期更改率統計Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RenewalStatsService {

    private final PolicyRepository policyRepository;

    /**
     * 早期更改率統計を取得
     *
     * @return 統計DTO
     */
    public RenewalStatsDto getStats() {
        log.debug("早期更改率統計を計算");

        LocalDate today = LocalDate.now();

        // 当年度の統計
        FiscalYearStats fiscalYear = calculateFiscalYearStats(today);

        // 当月の統計
        MonthStats month = calculateMonthStats(today);

        return RenewalStatsDto.builder()
                .fiscalYearStart(fiscalYear.startDate)
                .fiscalYearRate(fiscalYear.rateText)
                .fiscalYearCount(fiscalYear.countText)
                .monthStart(month.startDate)
                .monthRate(month.rateText)
                .monthCount(month.countText)
                .build();
    }

    /**
     * 当年度の早期更改率を計算
     *
     * 【修正】分母の絞り込み条件を変更
     *   旧: renewed_at が年度開始〜today の範囲（年度ズレが起きる可能性あり）
     *   新: renewal_due_end_date（nullならend_date）が当年度内にある契約で、
     *        かつ renewed_at が today 以前のもの
     *
     * @param today 今日の日付
     * @return 年度統計
     */
    private FiscalYearStats calculateFiscalYearStats(LocalDate today) {
        // 年度の開始日・終了日を計算（4月1日〜翌3月31日）
        LocalDate fiscalYearStart = getFiscalYearStart(today);
        LocalDate fiscalYearEnd = fiscalYearStart.plusYears(1).minusDays(1);

        log.debug("年度範囲: {} 〜 {}", fiscalYearStart, fiscalYearEnd);

        // 【修正】renewal_due_end_date が当年度内にある更新済み契約を取得
        List<Policy> renewedPolicies = policyRepository.findRenewedPoliciesInPeriod(
                fiscalYearStart, fiscalYearEnd, today);

        log.debug("当年度更新件数: {}", renewedPolicies.size());

        // 早期更改件数をカウント
        long earlyRenewalCount = renewedPolicies.stream()
                .filter(this::isEarlyRenewal)
                .count();

        log.debug("当年度早期更改件数: {}", earlyRenewalCount);

        // 表示用テキスト生成
        String startDateText = fiscalYearStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        FiscalYearStats stats = new FiscalYearStats();
        stats.startDate = startDateText;

        if (renewedPolicies.isEmpty()) {
            stats.rateText = "— (0/0)";
            stats.countText = "";
        } else {
            double rate = (double) earlyRenewalCount / renewedPolicies.size() * 100;
            stats.rateText = String.format("%.1f%% (%d/%d)",
                    rate, earlyRenewalCount, renewedPolicies.size());
            stats.countText = String.format("%d/%d", earlyRenewalCount, renewedPolicies.size());
        }

        return stats;
    }

    /**
     * 当月の早期更改率を計算
     *
     * 【修正】分母の絞り込み条件を当年度と同様に変更
     *
     * @param today 今日の日付
     * @return 月統計
     */
    private MonthStats calculateMonthStats(LocalDate today) {
        // 当月の開始日・終了日
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        log.debug("月範囲: {} 〜 {}", monthStart, monthEnd);

        // 【修正】renewal_due_end_date が当月内にある更新済み契約を取得
        List<Policy> renewedPolicies = policyRepository.findRenewedPoliciesInPeriod(
                monthStart, monthEnd, today);

        log.debug("当月更新件数: {}", renewedPolicies.size());

        // 早期更改件数をカウント
        long earlyRenewalCount = renewedPolicies.stream()
                .filter(this::isEarlyRenewal)
                .count();

        log.debug("当月早期更改件数: {}", earlyRenewalCount);

        // 表示用テキスト生成
        String startDateText = monthStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        MonthStats stats = new MonthStats();
        stats.startDate = startDateText;

        if (renewedPolicies.isEmpty()) {
            stats.rateText = "— (0/0)";
            stats.countText = "";
        } else {
            double rate = (double) earlyRenewalCount / renewedPolicies.size() * 100;
            stats.rateText = String.format("%.1f%% (%d/%d)",
                    rate, earlyRenewalCount, renewedPolicies.size());
            stats.countText = String.format("%d/%d", earlyRenewalCount, renewedPolicies.size());
        }

        return stats;
    }

    /**
     * 年度の開始日を計算（4月1日）
     *
     * @param date 基準日
     * @return 年度開始日
     */
    private LocalDate getFiscalYearStart(LocalDate date) {
        int year = date.getYear();
        // 4月1日より前なら前年度
        if (date.getMonthValue() < 4) {
            year--;
        }
        return LocalDate.of(year, 4, 1);
    }

    /**
     * 早期更改かどうか判定
     * 更新日が「満期21日前まで」に入っているかチェック
     *
     * @param policy 契約
     * @return true: 早期更改
     */
    private boolean isEarlyRenewal(Policy policy) {
        if (policy.getRenewedAt() == null || policy.getRenewalDueEndDate() == null) {
            return false;
        }

        LocalDate renewedDate = policy.getRenewedAt().toLocalDate();
        LocalDate originalEndDate = policy.getRenewalDueEndDate();

        // 早期更改期間の終了日（満期21日前）
        LocalDate earlyRenewalDeadline = originalEndDate.minusDays(21);

        // 更新日が早期更改期間内か判定
        // 満期2ヶ月前〜満期21日前
        LocalDate renewableStart = originalEndDate.minusMonths(2);

        return !renewedDate.isBefore(renewableStart) &&
                !renewedDate.isAfter(earlyRenewalDeadline);
    }

    /**
     * 年度統計の内部クラス
     */
    private static class FiscalYearStats {
        String startDate;
        String rateText;
        String countText;
    }

    /**
     * 月統計の内部クラス
     */
    private static class MonthStats {
        String startDate;
        String rateText;
        String countText;
    }
}
