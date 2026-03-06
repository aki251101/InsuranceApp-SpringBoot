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
 *
 * 事故（Accident）に関する業務ロジックを集約する。
 * 特に重要なのは「ステータス遷移ルール」の検証。
 *
 * 【ステータス遷移ルール（設計書準拠）】
 *   OPEN（受付）→ IN_PROGRESS（対応中）→ RESOLVED（完了）
 *   ※ RESOLVED からは戻せない（再OPENしない設計）
 *   ※ OPEN → RESOLVED への飛び越しも禁止（必ず IN_PROGRESS を経由する）
 *
 * 【トランザクション方針】
 *   - クラスレベル: @Transactional(readOnly = true) → 読み取り専用がデフォルト
 *   - 書き込みメソッド: 個別に @Transactional を付けて読み書き可能にする
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccidentService {

    // コンストラクタ注入（@RequiredArgsConstructor が final フィールドの
    // コンストラクタを自動生成する）
    private final AccidentRepository accidentRepository;

    /**
     * すべての事故を取得する（事故受付日が新しい順）
     *
     * @return 事故リスト
     */
    public List<Accident> getAllAccidents() {
        log.debug("すべての事故を取得");
        return accidentRepository.findAllOrderByOccurredAtDesc();
    }

    /**
     * IDで事故を取得する
     *
     * @param id 事故ID
     * @return 事故データ（Optional: 見つからない場合は empty）
     */
    public Optional<Accident> getAccidentById(Long id) {
        log.debug("事故を取得: id={}", id);
        return accidentRepository.findByIdWithPolicy(id);
    }

    /**
     * 対応中の事故を取得する（OPEN + IN_PROGRESS）
     *
     * 画面の「対応中」タブで使用する。
     * 受付直後（OPEN）と対応を開始した事故（IN_PROGRESS）をまとめて表示する。
     *
     * @return 事故リスト（受付日が新しい順）
     */
    public List<Accident> getOpenAndInProgressAccidents() {
        log.debug("対応中の事故を取得");
        return accidentRepository.findByStatusInOrderByOccurredAtDesc(
                Arrays.asList("OPEN", "IN_PROGRESS"));
    }

    /**
     * 完了した事故を取得する（RESOLVED）
     *
     * 画面の「完了」タブで使用する。
     *
     * @return 事故リスト（受付日が新しい順）
     */
    public List<Accident> getResolvedAccidents() {
        log.debug("完了した事故を取得");
        return accidentRepository.findByStatusOrderByOccurredAtDesc("RESOLVED");
    }

    /**
     * 契約IDで事故を取得する
     *
     * 契約詳細画面から「この契約に紐づく事故一覧」を表示する際に使用する。
     *
     * @param policyId 契約ID
     * @return 事故リスト
     */
    public List<Accident> getAccidentsByPolicyId(Long policyId) {
        log.debug("事故を取得: policyId={}", policyId);
        return accidentRepository.findByPolicyId(policyId);
    }

    /**
     * 事故を新規作成する
     *
     * status は @PrePersist で "OPEN" にデフォルト設定される。
     *
     * @param accident 事故データ（policyId, occurredAt, place, description を含む）
     * @return 保存された事故データ（idが採番済み）
     */
    @Transactional
    public Accident createAccident(Accident accident) {
        log.info("事故を作成: policyId={}", accident.getPolicyId());
        return accidentRepository.save(accident);
    }

    /**
     * 事故を更新する
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
     * 事故ステータスを変更する
     *
     * 【遷移ルール（設計書準拠・厳密チェック）】
     *   - 対応開始: OPEN → IN_PROGRESS のみ許可
     *   - 完了:     IN_PROGRESS → RESOLVED のみ許可
     *   - RESOLVED からは一切戻せない
     *   - OPEN → RESOLVED への飛び越しは禁止
     *
     * 不正な遷移を試みた場合は IllegalStateException を投げる。
     * 存在しない事故IDの場合は IllegalArgumentException を投げる。
     *
     * @param id        事故ID
     * @param newStatus 新しいステータス（"OPEN", "IN_PROGRESS", "RESOLVED"）
     * @return 更新された事故データ
     * @throws IllegalArgumentException 事故が見つからない場合、不正なステータス値の場合
     * @throws IllegalStateException    不正な遷移の場合
     */
    @Transactional
    public Accident changeStatus(Long id, String newStatus) {
        log.info("事故ステータス変更: id={}, newStatus={}", id, newStatus);

        // ① 事故を取得（見つからなければ例外）
        Accident accident = accidentRepository.findByIdWithPolicy(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "事故が見つかりません: id=" + id));

        String currentStatus = accident.getStatus();

        // ② ステータス値の妥当性チェック（許可された3値のみ）
        if (!Arrays.asList("OPEN", "IN_PROGRESS", "RESOLVED").contains(newStatus)) {
            throw new IllegalArgumentException("不正なステータスです: " + newStatus);
        }

        // ③ RESOLVED からは戻せない（再OPENしない設計）
        if ("RESOLVED".equals(currentStatus) && !"RESOLVED".equals(newStatus)) {
            throw new IllegalStateException("完了した事故は再開できません");
        }

        // ④ IN_PROGRESS への遷移は OPEN からのみ
        //    （IN_PROGRESS → IN_PROGRESS や RESOLVED → IN_PROGRESS は拒否）
        if ("IN_PROGRESS".equals(newStatus) && !"OPEN".equals(currentStatus)) {
            throw new IllegalStateException(
                    "対応開始は「受付」状態の事故のみ可能です（現在: "
                            + accident.getStatusLabel() + "）");
        }

        // ⑤ RESOLVED への遷移は IN_PROGRESS からのみ
        //    （OPEN → RESOLVED への飛び越しを防止）
        if ("RESOLVED".equals(newStatus) && !"IN_PROGRESS".equals(currentStatus)) {
            throw new IllegalStateException(
                    "完了は「対応中」状態の事故のみ可能です（現在: "
                            + accident.getStatusLabel() + "）");
        }

        // ⑥ ステータスを更新して保存
        accident.setStatus(newStatus);
        return accidentRepository.save(accident);
    }

    /**
     * 最終対応日を更新する（「対応した」ボタン押下時）
     *
     * 現在日時を lastContactedAt にセットする。
     * RESOLVED 状態の事故には更新できない（完了済みの事故を触らない設計）。
     *
     * @param id 事故ID
     * @return 更新された事故データ
     * @throws IllegalArgumentException 事故が見つからない場合
     * @throws IllegalStateException    RESOLVED の場合
     */
    @Transactional
    public Accident updateLastContactedAt(Long id) {
        log.info("最終対応日を更新: id={}", id);

        Accident accident = accidentRepository.findByIdWithPolicy(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "事故が見つかりません: id=" + id));

        // RESOLVED は更新不可
        if ("RESOLVED".equals(accident.getStatus())) {
            throw new IllegalStateException("完了した事故は対応日を更新できません");
        }

        accident.setLastContactedAt(LocalDateTime.now());
        return accidentRepository.save(accident);
    }

    /**
     * メモを更新する
     *
     * @param id   事故ID
     * @param memo 新しいメモ（null の場合は空文字に変換）
     * @return 更新された事故データ
     * @throws IllegalArgumentException 事故が見つからない場合
     */
    @Transactional
    public Accident updateMemo(Long id, String memo) {
        log.info("事故メモを更新: id={}", id);

        Accident accident = accidentRepository.findByIdWithPolicy(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "事故が見つかりません: id=" + id));

        // null 防止: null が来たら空文字にする
        accident.setMemo(memo != null ? memo : "");
        return accidentRepository.save(accident);
    }

    /**
     * 対応開始のショートカット（OPEN → IN_PROGRESS）
     *
     * 内部で changeStatus を呼び出す。
     * Controller から呼びやすいように用意したヘルパーメソッド。
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
     * 完了のショートカット（IN_PROGRESS → RESOLVED）
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

