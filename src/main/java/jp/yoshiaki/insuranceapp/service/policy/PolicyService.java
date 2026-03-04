package jp.yoshiaki.insuranceapp.service.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.repository.policy.PolicyRepository;
import jp.yoshiaki.insuranceapp.service.calendar.CalendarClient;
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
 *
 * 【Day93で追加/修正】
 * - CalendarService 依存を廃止し、CalendarClient（interface）に依存
 * - toggleCalendarRegistration(): カレンダー登録トグル（ON/OFF）
 * - unrenewPolicy(): 更新取消時にカレンダー登録がONならイベント削除＋OFF化
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;

    // 【Day93】Stub/本番を @Profile で差し替えるため、interface に依存する
    private final CalendarClient calendarClient;

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
     */
    @Transactional
    public Policy renewPolicy(Long id) {
        log.info("契約を更新（renew）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        if (!policy.isRenewable()) {
            throw new IllegalStateException("更新可能期間外のため更新できません");
        }

        policy.setRenewalDueEndDate(policy.getEndDate());
        policy.setEndDate(policy.getEndDate().plusYears(1));
        policy.setRenewedAt(LocalDateTime.now());

        return policyRepository.save(policy);
    }

    /**
     * 契約更新を取り消し（当日限定）
     */
    @Transactional
    public Policy unrenewPolicy(Long id) {
        log.info("契約更新を取り消し（unrenew）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        if (policy.getRenewedAt() == null) {
            throw new IllegalStateException("更新されていない契約です");
        }

        LocalDate renewedDate = policy.getRenewedAt().toLocalDate();
        if (!renewedDate.equals(LocalDate.now())) {
            throw new IllegalStateException("更新取消は当日のみ可能です");
        }

        if (policy.getRenewalDueEndDate() == null) {
            throw new IllegalStateException("更新前満期日が見つかりません");
        }

        // 【Day93】カレンダー登録がONの場合、イベント削除＋登録状態OFFに戻す
        if (Boolean.TRUE.equals(policy.getCalendarRegistered())) {
            log.info("更新取消に伴いカレンダーイベントを削除: policyId={}", id);
            if (policy.getCalendarEventId() != null) {
                calendarClient.deleteEvent(policy.getCalendarEventId());
            }
            policy.setCalendarRegistered(false);
            policy.setCalendarEventId(null);
        }

        policy.setEndDate(policy.getRenewalDueEndDate());
        policy.setRenewalDueEndDate(null);
        policy.setRenewedAt(null);

        return policyRepository.save(policy);
    }

    /**
     * 契約を解約
     */
    @Transactional
    public Policy cancelPolicy(Long id) {
        log.info("契約を解約（cancel）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        if (!"契約中".equals(policy.getEffectiveStatus())) {
            throw new IllegalStateException("解約できない状態です");
        }

        policy.setStatus("CANCELLED");
        policy.setCancelledAt(LocalDateTime.now());

        return policyRepository.save(policy);
    }

    /**
     * 契約解約を取り消し（当日限定）
     */
    @Transactional
    public Policy uncancelPolicy(Long id) {
        log.info("契約解約を取り消し（uncancel）: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

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

    // ─── Day93で追加するメソッド ───

    /**
     * カレンダー登録をトグル
     *
     * effectiveStatus が「契約中」のときのみ操作可能。
     * ON → OFF：イベント削除＋フラグOFF
     * OFF → ON：イベント作成＋フラグON＋eventId保存
     */
    @Transactional
    public Policy toggleCalendarRegistration(Long id) {
        log.info("カレンダー登録トグル: id={}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません"));

        if (!"契約中".equals(policy.getEffectiveStatus())) {
            throw new IllegalStateException("契約中の契約のみカレンダー登録できます");
        }

        if (policy.getCalendarRegistered()) {
            if (policy.getCalendarEventId() != null) {
                calendarClient.deleteEvent(policy.getCalendarEventId());
            }
            policy.setCalendarRegistered(false);
            policy.setCalendarEventId(null);
        } else {
            String eventId = calendarClient.createEvent(policy);
            policy.setCalendarRegistered(true);
            policy.setCalendarEventId(eventId);
        }

        return policyRepository.save(policy);
    }
}
