package jp.yoshiaki.insuranceapp.service.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.repository.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 契約Service
 * 契約に関するビジネスロジックを実装
 *
 * 【Day92で追加】
 * - renewPolicy()     : 更新（満期日1年延長）
 * - unrenewPolicy()   : 更新取消（当日限定）
 * - cancelPolicy()    : 解約（effectiveStatusが契約中の場合のみ）
 * - uncancelPolicy()  : 解約取消（当日限定）
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;

    // ─── Day91で作成済みのメソッド（既存） ───

    /**
     * すべての契約を取得
     */
    public List<Policy> getAllPolicies() {
        log.debug("すべての契約を取得");
        return policyRepository.findAll();
    }

    /**
     * IDで契約を取得
     */
    public Optional<Policy> getPolicyById(Long id) {
        log.debug("契約を取得: id={}", id);
        return policyRepository.findById(id);
    }

    /**
     * 契約を新規作成
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        log.info("契約を作成: policyNumber={}", policy.getPolicyNumber());
        return policyRepository.save(policy);
    }

    /**
     * 契約を保存（汎用の更新メソッド）
     */
    @Transactional
    public Policy updatePolicy(Policy policy) {
        log.info("契約を更新: id={}", policy.getId());
        return policyRepository.save(policy);
    }

    // ─── Day92で追加するメソッド ───

    /**
     * 契約を更新（満期日を1年延長）
     *
     * 【処理の流れ】
     *   1. IDで契約を取得（なければ例外）
     *   2. isRenewable() でガードチェック（期間外なら例外）
     *   3. 更新前満期日を renewalDueEndDate に退避（取消に備える）
     *   4. 満期日を1年延長
     *   5. 更新日時(renewedAt)を記録（当日取消の判定に使用）
     *   6. DBに保存
     *
     * @param id 契約ID
     * @return 更新済みの契約
     * @throws IllegalArgumentException 契約が見つからない場合
     * @throws IllegalStateException 更新可能期間外の場合
     */
    @Transactional
    public Policy renewPolicy(Long id) {
        log.info("契約を更新（renew）: id={}", id);

        // ① IDで契約を取得
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        // ② 更新可能期間のガードチェック
        if (!policy.isRenewable()) {
            throw new IllegalStateException("更新可能期間外のため更新できません");
        }

        // ③ 更新前満期日を退避（取消用）
        policy.setRenewalDueEndDate(policy.getEndDate());

        // ④ 満期日を1年延長
        policy.setEndDate(policy.getEndDate().plusYears(1));

        // ⑤ 更新操作日時を記録
        policy.setRenewedAt(LocalDateTime.now());

        // ⑥ DBに保存して返す
        return policyRepository.save(policy);
    }

    /**
     * 契約更新を取り消し（当日限定）
     *
     * 【処理の流れ】
     *   1. IDで契約を取得
     *   2. renewedAt が null でないことを確認（更新済みかチェック）
     *   3. renewedAt が今日であることを確認（当日限定）
     *   4. renewalDueEndDate（旧満期日）が存在することを確認
     *   5. 満期日を元に戻す
     *   6. 退避データ（renewalDueEndDate / renewedAt）をクリア
     *   7. DBに保存
     *
     * @param id 契約ID
     * @return 取消済みの契約
     * @throws IllegalArgumentException 契約が見つからない場合
     * @throws IllegalStateException 当日以外、未更新、旧満期日がない場合
     */
    @Transactional
    public Policy unrenewPolicy(Long id) {
        log.info("契約更新を取り消し（unrenew）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        // ガード①：そもそも更新されているか
        if (policy.getRenewedAt() == null) {
            throw new IllegalStateException("更新されていない契約です");
        }

        // ガード②：更新操作が今日か（当日限定）
        LocalDate renewedDate = policy.getRenewedAt().toLocalDate();
        if (!renewedDate.equals(LocalDate.now())) {
            throw new IllegalStateException("更新取消は当日のみ可能です");
        }

        // ガード③：旧満期日が退避されているか
        if (policy.getRenewalDueEndDate() == null) {
            throw new IllegalStateException("更新前満期日が見つかりません");
        }

        // 満期日を元に戻す
        policy.setEndDate(policy.getRenewalDueEndDate());

        // 退避データをクリア
        policy.setRenewalDueEndDate(null);
        policy.setRenewedAt(null);

        return policyRepository.save(policy);
    }

    /**
     * 契約を解約
     *
     * 【ガード条件】
     *   effectiveStatus が「契約中」でなければ解約不可。
     *   → 「失効」（満期切れ）の契約は解約できない（すでに無効なので）
     *   → 「解約」（解約済み）の契約も重複解約できない
     *
     * @param id 契約ID
     * @return 解約済みの契約
     * @throws IllegalArgumentException 契約が見つからない場合
     * @throws IllegalStateException effectiveStatusが契約中でない場合
     */
    @Transactional
    public Policy cancelPolicy(Long id) {
        log.info("契約を解約（cancel）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        // effectiveStatus が「契約中」でなければ解約不可
        if (!"契約中".equals(policy.getEffectiveStatus())) {
            throw new IllegalStateException("解約できない状態です");
        }

        // ステータスを CANCELLED に変更
        policy.setStatus("CANCELLED");

        // 解約操作日時を記録（当日取消の判定に使用）
        policy.setCancelledAt(LocalDateTime.now());

        return policyRepository.save(policy);
    }

    /**
     * 契約解約を取り消し（当日限定）
     *
     * 【処理の流れ】
     *   1. 解約日時（cancelledAt）が記録されていることを確認
     *   2. 解約日が今日であることを確認（当日限定）
     *   3. ステータスを ACTIVE に戻す
     *   4. 解約日時をクリア
     *
     * @param id 契約ID
     * @return 取消済みの契約
     * @throws IllegalArgumentException 契約が見つからない場合
     * @throws IllegalStateException 当日以外、未解約の場合
     */
    @Transactional
    public Policy uncancelPolicy(Long id) {
        log.info("契約解約を取り消し（uncancel）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        // ガード①：解約済みか
        if (policy.getCancelledAt() == null) {
            throw new IllegalStateException("解約されていない契約です");
        }

        // ガード②：解約操作が今日か（当日限定）
        LocalDate cancelledDate = policy.getCancelledAt().toLocalDate();
        if (!cancelledDate.equals(LocalDate.now())) {
            throw new IllegalStateException("解約取消は当日のみ可能です");
        }

        // ステータスを ACTIVE に戻す
        policy.setStatus("ACTIVE");

        // 解約日時をクリア
        policy.setCancelledAt(null);

        return policyRepository.save(policy);
    }
}
