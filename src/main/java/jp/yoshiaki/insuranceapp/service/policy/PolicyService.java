// 配置：src/main/java/jp/yoshiaki/insuranceapp/service/policy/PolicyService.java
package jp.yoshiaki.insuranceapp.service.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.repository.policy.PolicyRepository;
import jp.yoshiaki.insuranceapp.util.NormalizationUtil;
import jp.yoshiaki.insuranceapp.util.PolicyNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 契約 Service。
 * 契約に関するビジネスロジックをこのクラスに集約する。
 *
 * クラスレベルで @Transactional(readOnly = true) を付けている理由：
 *   → このクラスのメソッドは「読み取り専用」がデフォルト
 *   → 書き込み（INSERT/UPDATE）が必要なメソッドだけ @Transactional で上書きする
 *   → readOnly = true にすると、JPA が「変更検知」をスキップして少し速くなる
 *
 * Day91 スコープ：createPolicy / getAllPolicies / getPolicyById / searchPolicies
 * Day92 で追加予定：renewPolicy / cancelPolicy / unrenewPolicy / uncancelPolicy
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    /** DB アクセス窓口 */
    private final PolicyRepository policyRepository;

    /** 契約番号の自動附番ユーティリティ */
    private final PolicyNumberGenerator policyNumberGenerator;

    // ── 取得系（readOnly = true のまま） ──────────────────────

    /**
     * すべての契約を取得する。
     *
     * @return 全契約リスト
     */
    public List<Policy> getAllPolicies() {
        log.debug("すべての契約を取得");
        return policyRepository.findAll();
    }

    /**
     * ID を指定して契約を1件取得する。
     * 見つからなければ NotFoundException をスローする。
     *
     * @param id 契約ID
     * @return Policy エンティティ
     * @throws NotFoundException 該当IDの契約が存在しない場合
     */
    public Policy getPolicyById(Long id) {
        log.debug("契約を取得: id={}", id);
        return policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "契約が見つかりません: id=" + id));
    }

    /**
     * 契約番号 または 契約者名で部分一致検索する。
     * キーワードが空の場合は全件取得にフォールバックする。
     *
     * @param query 検索キーワード
     * @return 該当する契約リスト
     */
    public List<Policy> searchPolicies(String query) {
        log.debug("契約を検索: query={}", query);

        // キーワードが空なら全件返す
        if (query == null || query.isBlank()) {
            return getAllPolicies();
        }

        // 全角/半角の揺れを統一してから検索する
        String normalized = NormalizationUtil.normalizeSearchKeyword(query);

        return policyRepository
                .findByPolicyNumberContainingOrCustomerNameContaining(
                        normalized, normalized);
    }

    /**
     * 更新可能な契約を取得する（ACTIVE かつ 満期日が今日〜2ヶ月後）。
     *
     * @return 更新可能な契約リスト
     */
    public List<Policy> getRenewablePolicies() {
        log.debug("更新可能契約を取得");
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        return policyRepository.findRenewablePolicies(today, twoMonthsLater);
    }

    /**
     * ステータス指定で契約を取得する。
     * ACTIVE の場合は失効済みを除外した「真の契約中」のみ返す。
     *
     * @param status ステータス（"ACTIVE" / "CANCELLED"）
     * @return 契約リスト
     */
    public List<Policy> getPoliciesByStatus(String status) {
        log.debug("契約を検索: status={}", status);
        if ("ACTIVE".equals(status)) {
            return policyRepository.findActivePolicies(LocalDate.now());
        }
        return policyRepository
                .findByStatusOrderByEndDateAscPolicyNumberAsc(status);
    }

    /**
     * 失効契約を取得する（満期日が過ぎていて、かつ解約でない）。
     *
     * @return 失効した契約リスト
     */
    public List<Policy> getLapsedPolicies() {
        log.debug("失効契約を取得");
        return policyRepository.findLapsedPolicies(LocalDate.now());
    }

    // ── 作成系（@Transactional で readOnly を上書き） ──────────

    /**
     * 契約を新規作成する。
     *
     * 処理の流れ：
     *   ① 契約番号を自動附番する（例: P-2026-0001）
     *   ② 満期日を自動計算する（開始日 + 1年）
     *   ③ 契約者名を正規化する（全角/半角統一）
     *   ④ DB に保存する（@PrePersist で created_at / updated_at が自動設定される）
     *
     * @param policy Policy エンティティ（customerName, startDate は必須）
     * @return 保存済みの Policy エンティティ（id が採番された状態）
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        // ① 契約番号を自動附番
        String generatedNumber = policyNumberGenerator.generate(policy.getStartDate());
        policy.setPolicyNumber(generatedNumber);
        log.info("契約を作成: policyNumber={}", generatedNumber);

        // ② 満期日を自動計算（開始日 + 1年）
        LocalDate endDate = calculateEndDate(policy.getStartDate());
        policy.setEndDate(endDate);
        log.debug("満期日を自動計算: startDate={}, endDate={}",
                policy.getStartDate(), endDate);

        // ③ 契約者名を正規化
        policy.setCustomerName(
                NormalizationUtil.normalizeCustomerName(policy.getCustomerName()));

        // ④ DB に保存（@PrePersist が created_at/updated_at/status/calendarRegistered を設定）
        return policyRepository.save(policy);
    }

    // ── private メソッド ──────────────────────

    /**
     * 満期日を自動計算する。
     *
     * ルール：
     *   基本：開始日 + 1年（同じ月日）
     *   例外：開始日が2月29日で翌年が平年の場合 → 2月28日
     *   ※ Java の LocalDate.plusYears(1) が上記を自動で処理してくれる
     *
     * @param startDate 契約開始日
     * @return 満期日
     */
    private LocalDate calculateEndDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("開始日が設定されていません");
        }
        return startDate.plusYears(1);
    }
}
