package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.repository.PolicyRepository;
import jp.yoshiaki.insuranceapp.util.NormalizationUtil;
import jp.yoshiaki.insuranceapp.util.PolicyNumberGenerator;
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
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final CalendarService calendarService;
    private final PolicyNumberGenerator policyNumberGenerator;

    /**
     * すべての契約を取得（満期日が近い順）
     *
     * @return 契約リスト
     */
    public List<Policy> getAllPolicies() {
        log.debug("すべての契約を取得");
        return policyRepository.findAll();
    }

    /**
     * IDで契約を取得
     *
     * @param id 契約ID
     * @return 契約データ
     */
    public Optional<Policy> getPolicyById(Long id) {
        log.debug("契約を取得: id={}", id);
        return policyRepository.findById(id);
    }

    /**
     * 契約番号で契約を取得
     *
     * @param policyNumber 契約番号
     * @return 契約データ
     */
    public Optional<Policy> getPolicyByNumber(String policyNumber) {
        log.debug("契約を取得: policyNumber={}", policyNumber);
        return policyRepository.findByPolicyNumber(policyNumber);
    }

    /**
     * 更新可能契約を取得
     * ステータスがACTIVEで、満期2ヶ月前〜満期日
     *
     * 【修正】パラメータを twoMonthsBefore → twoMonthsLater に変更
     *   旧: today.minusMonths(2) を渡していた（過去方向）
     *   新: today.plusMonths(2) を渡す（未来方向・満期日の上限）
     *
     * @return 契約リスト
     */
    public List<Policy> getRenewablePolicies() {
        log.debug("更新可能契約を取得");
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        return policyRepository.findRenewablePolicies(today, twoMonthsLater);
    }

    /**
     * 【修正1】ステータスで契約を検索
     *
     *   旧: 全ステータス共通で findByStatusOrderByEndDateAscPolicyNumberAsc を使用
     *        → ACTIVE指定時、endDate < today（失効）の契約も含まれていた
     *   新: ACTIVE の場合は findActivePolicies を使用し、満期日未到来の契約のみ返す
     *        → 「契約中」タブには失効済みの契約が表示されなくなる
     *
     * @param status ステータス
     * @return 契約リスト
     */
    public List<Policy> getPoliciesByStatus(String status) {
        log.debug("契約を検索: status={}", status);
        if ("ACTIVE".equals(status)) {
            return policyRepository.findActivePolicies(LocalDate.now());
        }
        return policyRepository.findByStatusOrderByEndDateAscPolicyNumberAsc(status);
    }

    /**
     * 失効契約を取得
     * 満期日が過ぎていて、かつ解約ではない契約
     *
     * @return 契約リスト
     */
    public List<Policy> getLapsedPolicies() {
        log.debug("失効契約を取得");
        return policyRepository.findLapsedPolicies(LocalDate.now());
    }

    /**
     * 契約番号または契約者名で検索
     *
     * @param query 検索キーワード
     * @return 契約リスト
     */
    public List<Policy> searchPolicies(String query) {
        log.debug("契約を検索: query={}", query);
        if (query == null || query.isBlank()) {
            return getAllPolicies();
        }

        // 検索キーワードを正規化
        String normalized = NormalizationUtil.normalizeSearchKeyword(query);

        return policyRepository.findByPolicyNumberContainingOrCustomerNameContaining(
                normalized, normalized);
    }

    /**
     * 【修正2・3】契約を新規作成（契約番号自動附番＋満期日自動計算）
     *
     *   旧: 契約番号をユーザーが入力し、満期日もユーザーが入力する仕様
     *   新:
     *     ・契約番号は年度ごとの連番で自動附番（例: P-2026-0001）
     *     ・満期日は開始日の1年後を自動計算
     *       - 基本: 開始日 + 1年（同じ月日）
     *       - 例外: 開始日が2月29日で翌年が平年の場合 → 2月28日
     *
     * @param policy 契約データ（customerName, startDate は必須。policyNumber, endDate は自動設定）
     * @return 保存された契約データ
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        // 契約番号を自動附番
        String generatedNumber = policyNumberGenerator.generate(policy.getStartDate());
        policy.setPolicyNumber(generatedNumber);
        log.info("契約を作成: policyNumber={}", generatedNumber);

        // 満期日を自動計算（開始日 + 1年）
        LocalDate endDate = calculateEndDate(policy.getStartDate());
        policy.setEndDate(endDate);
        log.debug("満期日を自動計算: startDate={}, endDate={}", policy.getStartDate(), endDate);

        // 契約者名を正規化
        policy.setCustomerName(
                NormalizationUtil.normalizeCustomerName(policy.getCustomerName()));

        return policyRepository.save(policy);
    }

    /**
     * 【修正3】満期日を自動計算
     *
     * 計算ルール:
     *   基本: 開始日 + 1年（同じ月日）
     *   例外: 開始日が2月29日で、翌年が平年の場合のみ → 2月28日
     *   ※ Java の LocalDate.plusYears(1) は上記ルールを自動で処理する
     *      （2月29日 + 1年 → 平年なら2月28日に自動調整）
     *
     * @param startDate 契約開始日
     * @return 満期日（開始日の1年後）
     */
    private LocalDate calculateEndDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("開始日が設定されていません");
        }
        return startDate.plusYears(1);
    }

    /**
     * 契約を更新
     *
     * @param policy 契約データ
     * @return 更新された契約データ
     */
    @Transactional
    public Policy updatePolicy(Policy policy) {
        log.info("契約を更新: id={}", policy.getId());
        return policyRepository.save(policy);
    }

    /**
     * 契約を更新（更新ボタン押下時）
     *
     * @param id 契約ID
     * @return 更新された契約データ
     * @throws IllegalStateException 更新可能期間外の場合
     */
    @Transactional
    public Policy renewPolicy(Long id) {
        log.info("契約を更新: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        // 更新可能かチェック
        if (!policy.isRenewable()) {
            throw new IllegalStateException("更新可能期間外のため更新できません");
        }

        // 更新前満期日を退避
        policy.setRenewalDueEndDate(policy.getEndDate());

        // 満期日を1年延長
        policy.setEndDate(policy.getEndDate().plusYears(1));

        // 更新日時を記録
        policy.setRenewedAt(LocalDateTime.now());

        return policyRepository.save(policy);
    }

    /**
     * 契約更新を取り消し（当日限定）
     *
     * 【修正】カレンダー登録がONの場合、イベント削除＋登録状態OFFに戻す処理を追加
     *   設計書: 「更新取消時、カレンダー登録がONならイベントは削除する
     *           （登録状態もOFFに戻す）。再度必要なら利用者が再登録する。」
     *
     * @param id 契約ID
     * @return 更新された契約データ
     * @throws IllegalStateException 当日以外、または更新前満期日がない場合
     */
    @Transactional
    public Policy unrenewPolicy(Long id) {
        log.info("契約更新を取り消し: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        // 更新日時が当日かチェック
        if (policy.getRenewedAt() == null) {
            throw new IllegalStateException("更新されていない契約です");
        }

        LocalDate renewedDate = policy.getRenewedAt().toLocalDate();
        if (!renewedDate.equals(LocalDate.now())) {
            throw new IllegalStateException("更新取消は当日のみ可能です");
        }

        // 更新前満期日があるかチェック
        if (policy.getRenewalDueEndDate() == null) {
            throw new IllegalStateException("更新前満期日が見つかりません");
        }

        // 【修正追加】カレンダー登録がONの場合、イベント削除＋登録状態OFF
        if (Boolean.TRUE.equals(policy.getCalendarRegistered())) {
            log.info("更新取消に伴いカレンダーイベントを削除: policyId={}", id);
            if (policy.getCalendarEventId() != null) {
                calendarService.deleteEvent(policy.getCalendarEventId());
            }
            policy.setCalendarRegistered(false);
            policy.setCalendarEventId(null);
        }

        // 満期日を元に戻す
        policy.setEndDate(policy.getRenewalDueEndDate());
        policy.setRenewalDueEndDate(null);
        policy.setRenewedAt(null);

        return policyRepository.save(policy);
    }

    /**
     * 契約を解約
     *
     * 【修正】ガード条件を effectiveStatus ベースに変更
     *   旧: status == ACTIVE のみチェック（失効済みでも解約可能だった）
     *   新: effectiveStatus が「契約中」のときのみ解約可能（失効済みは不可）
     *
     * @param id 契約ID
     * @return 更新された契約データ
     * @throws IllegalStateException effectiveStatusがACTIVEでない場合
     */
    @Transactional
    public Policy cancelPolicy(Long id) {
        log.info("契約を解約: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        // effectiveStatus が「契約中」でなければ解約不可
        if (!"契約中".equals(policy.getEffectiveStatus())) {
            throw new IllegalStateException("解約できない状態です");
        }

        policy.setStatus("CANCELLED");
        policy.setCancelledAt(LocalDateTime.now());

        return policyRepository.save(policy);
    }

    /**
     * 契約解約を取り消し（当日限定）
     *
     * @param id 契約ID
     * @return 更新された契約データ
     * @throws IllegalStateException 当日以外の場合
     */
    @Transactional
    public Policy uncancelPolicy(Long id) {
        log.info("契約解約を取り消し: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        // 解約日時が当日かチェック
        if (policy.getCancelledAt() == null) {
            throw new IllegalStateException("解約されていない契約です");
        }

        LocalDate cancelledDate = policy.getCancelledAt().toLocalDate();
        if (!cancelledDate.equals(LocalDate.now())) {
            throw new IllegalStateException("解約取消は当日のみ可能です");
        }

        policy.setStatus("ACTIVE");
        policy.setCancelledAt(null);

        return policyRepository.save(policy);
    }

    /**
     * カレンダー登録をトグル
     *
     * 【修正】ガード条件を effectiveStatus ベースに変更
     *   旧: status == ACTIVE のみチェック（失効済みでも登録可能だった）
     *   新: effectiveStatus が「契約中」のときのみ登録可能
     *
     * @param id 契約ID
     * @return 更新された契約データ
     */
    @Transactional
    public Policy toggleCalendarRegistration(Long id) {
        log.info("カレンダー登録トグル: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        // effectiveStatus が「契約中」でなければ登録不可
        if (!"契約中".equals(policy.getEffectiveStatus())) {
            throw new IllegalStateException("契約中の契約のみカレンダー登録できます");
        }

        if (policy.getCalendarRegistered()) {
            // 登録済み → 未登録に変更（イベント削除）
            if (policy.getCalendarEventId() != null) {
                calendarService.deleteEvent(policy.getCalendarEventId());
            }
            policy.setCalendarRegistered(false);
            policy.setCalendarEventId(null);
        } else {
            // 未登録 → 登録に変更（イベント作成）
            String eventId = calendarService.createEvent(policy);
            policy.setCalendarRegistered(true);
            policy.setCalendarEventId(eventId);
        }

        return policyRepository.save(policy);
    }
}


