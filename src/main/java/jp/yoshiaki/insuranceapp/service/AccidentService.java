package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.repository.AccidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 事故Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccidentService {

    private final AccidentRepository accidentRepository;

    /**
     * すべての事故を取得（事故受付日が新しい順）
     *
     * @return 事故リスト
     */
    public List<Accident> getAllAccidents() {
        log.debug("すべての事故を取得");
        return accidentRepository.findAllOrderByOccurredAtDesc();
    }

    /**
     * IDで事故を取得
     *
     * @param id 事故ID
     * @return 事故データ
     */
    public Optional<Accident> getAccidentById(Long id) {
        log.debug("事故を取得: id={}", id);
        return accidentRepository.findById(id);
    }

    /**
     * 対応中の事故を取得（OPEN + IN_PROGRESS）
     *
     * @return 事故リスト
     */
    public List<Accident> getOpenAndInProgressAccidents() {
        log.debug("対応中の事故を取得");
        return accidentRepository.findByStatusInOrderByOccurredAtDesc(
                Arrays.asList("OPEN", "IN_PROGRESS"));
    }

    /**
     * 完了した事故を取得（RESOLVED）
     *
     * @return 事故リスト
     */
    public List<Accident> getResolvedAccidents() {
        log.debug("完了した事故を取得");
        return accidentRepository.findByStatusOrderByOccurredAtDesc("RESOLVED");
    }

    /**
     * 契約IDで事故を取得
     *
     * @param policyId 契約ID
     * @return 事故リスト
     */
    public List<Accident> getAccidentsByPolicyId(Long policyId) {
        log.debug("事故を取得: policyId={}", policyId);
        return accidentRepository.findByPolicyId(policyId);
    }

    /**
     * 事故を新規作成
     *
     * @param accident 事故データ
     * @return 保存された事故データ
     */
    @Transactional
    public Accident createAccident(Accident accident) {
        log.info("事故を作成: policyId={}", accident.getPolicyId());
        return accidentRepository.save(accident);
    }

    /**
     * 事故を更新
     *
     * @param accident 事故データ
     * @return 更新された事故データ
     */
    @Transactional
    public Accident updateAccident(Accident accident) {
        log.info("事故を更新: id={}", accident.getId());
        return accidentRepository.save(accident);
    }

    /**
     * 事故ステータスを変更
     *
     * 【修正】ステータス遷移ルールを厳密化
     *   旧: RESOLVEDからの逆戻りのみ禁止（OPEN→RESOLVEDが可能だった）
     *   新: 設計書に準拠した遷移ルールを追加
     *        - 対応開始: OPEN → IN_PROGRESS のみ
     *        - 完了: IN_PROGRESS → RESOLVED のみ
     *        - RESOLVEDからは戻せない
     *
     * @param id 事故ID
     * @param newStatus 新しいステータス
     * @return 更新された事故データ
     * @throws IllegalStateException 不正な遷移の場合
     */
    @Transactional
    public Accident changeStatus(Long id, String newStatus) {
        log.info("事故ステータス変更: id={}, newStatus={}", id, newStatus);

        Accident accident = accidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("事故が見つかりません"));

        String currentStatus = accident.getStatus();

        // ステータス検証
        if (!Arrays.asList("OPEN", "IN_PROGRESS", "RESOLVED").contains(newStatus)) {
            throw new IllegalArgumentException("不正なステータスです: " + newStatus);
        }

        // RESOLVEDからは戻せない
        if ("RESOLVED".equals(currentStatus) && !"RESOLVED".equals(newStatus)) {
            throw new IllegalStateException("完了した事故は再開できません");
        }

        // 【修正追加】遷移ルールの厳密チェック
        // IN_PROGRESS への遷移は OPEN からのみ
        if ("IN_PROGRESS".equals(newStatus) && !"OPEN".equals(currentStatus)) {
            throw new IllegalStateException(
                    "対応開始は「受付」状態の事故のみ可能です（現在: " + accident.getStatusLabel() + "）");
        }

        // RESOLVED への遷移は IN_PROGRESS からのみ
        if ("RESOLVED".equals(newStatus) && !"IN_PROGRESS".equals(currentStatus)) {
            throw new IllegalStateException(
                    "完了は「対応中」状態の事故のみ可能です（現在: " + accident.getStatusLabel() + "）");
        }

        accident.setStatus(newStatus);
        return accidentRepository.save(accident);
    }

    /**
     * 最終対応日を更新（「対応した」ボタン）
     *
     * @param id 事故ID
     * @return 更新された事故データ
     * @throws IllegalStateException RESOLVEDの場合
     */
    @Transactional
    public Accident updateLastContactedAt(Long id) {
        log.info("最終対応日を更新: id={}", id);

        Accident accident = accidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("事故が見つかりません"));

        // RESOLVEDは更新不可
        if ("RESOLVED".equals(accident.getStatus())) {
            throw new IllegalStateException("完了した事故は対応日を更新できません");
        }

        accident.setLastContactedAt(LocalDateTime.now());
        return accidentRepository.save(accident);
    }

    /**
     * メモを更新
     *
     * @param id 事故ID
     * @param memo 新しいメモ
     * @return 更新された事故データ
     */
    @Transactional
    public Accident updateMemo(Long id, String memo) {
        log.info("事故メモを更新: id={}", id);

        Accident accident = accidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("事故が見つかりません"));

        accident.setMemo(memo != null ? memo : "");
        return accidentRepository.save(accident);
    }

    /**
     * 対応開始（OPEN → IN_PROGRESS）
     *
     * @param id 事故ID
     * @return 更新された事故データ
     */
    @Transactional
    public Accident startProgress(Long id) {
        log.info("事故対応開始: id={}", id);
        return changeStatus(id, "IN_PROGRESS");
    }

    /**
     * 完了（IN_PROGRESS → RESOLVED）
     *
     * @param id 事故ID
     * @return 更新された事故データ
     */
    @Transactional
    public Accident resolve(Long id) {
        log.info("事故完了: id={}", id);
        return changeStatus(id, "RESOLVED");
    }
}
