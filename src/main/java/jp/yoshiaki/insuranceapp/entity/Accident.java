package jp.yoshiaki.insuranceapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 事故エンティティ
 * accidents テーブルに対応（ER図準拠）
 *
 * ステータス遷移: OPEN → IN_PROGRESS → RESOLVED（一方通行）
 * 滞留判定: 最終対応日から7日以上経過 かつ 未完了 → 滞留
 */
@Entity
@Table(name = "accidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accident {

    // ① 事故ID（主キー・自動採番）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ② 契約ID（外部キー：policiesテーブルのidを参照）
    //    INSERT/UPDATE時はこの数値カラムを直接操作する
    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    // ③ 契約情報（Policyエンティティとのリレーション）
    //    FetchType.LAZY: 必要な時だけDBから読み込む（性能対策）
    //    insertable=false, updatable=false: このオブジェクト経由ではINSERT/UPDATEしない
    //    → policyId(②) と policy(③) で同じカラムを二重管理しないための設定
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private Policy policy;

    // ④ 事故受付日
    @Column(name = "occurred_at", nullable = false)
    private LocalDate occurredAt;

    // ⑤ 事故場所
    @Column(name = "place", length = 200)
    private String place;

    // ⑥ 事故概要
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ⑦ 事故状態: OPEN（受付）, IN_PROGRESS（対応中）, RESOLVED（完了）
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    // ⑧ 最終対応日時（滞留判定に使用）
    //    「対応した」ボタン押下時に現在日時をセットする
    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    // ⑨ 対応履歴メモ（自由記述テキスト）
    @Builder.Default
    @Column(name = "memo", columnDefinition = "TEXT", nullable = false)
    private String memo = "";

    // ⑩ 作成日時（初回保存時に自動設定、以降更新しない）
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⑪ 更新日時（保存・更新のたびに自動更新）
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティ保存前に自動実行（初回INSERT時）
     * - 作成日時・更新日時を現在時刻で初期化
     * - ステータス未設定なら OPEN（受付）をデフォルトセット
     * - メモ未設定なら空文字をセット（null防止）
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "OPEN";
        }
        if (memo == null) {
            memo = "";
        }
    }

    /**
     * エンティティ更新前に自動実行（UPDATE時）
     * - 更新日時を現在時刻で上書き
     */
    @PreUpdate
    protected void onUpdate() {
        if (memo == null) {
            memo = "";
        }
        updatedAt = LocalDateTime.now();
    }

    /**
     * 事故ステータスの日本語ラベルを返す（表示用）
     *
     * 内部値（英語） → 表示値（日本語）の変換
     * OPEN → 受付, IN_PROGRESS → 対応中, RESOLVED → 完了
     *
     * @return 日本語のステータスラベル
     */
    public String getStatusLabel() {
        return switch (status) {
            case "OPEN" -> "受付";
            case "IN_PROGRESS" -> "対応中";
            case "RESOLVED" -> "完了";
            default -> status;
        };
    }

    /**
     * 滞留しているかどうかを判定する
     *
     * 【判定ルール】
     * - RESOLVED（完了）なら滞留しない → false
     * - lastContactedAt が null（一度も対応していない）→ occurredAt（受付日）から7日以上で滞留
     * - lastContactedAt がある → その日時から7日以上経過で滞留
     *
     * 【境界条件の注意】
     * - 設計書:「last_contacted_at <= today - 7日」= 7日以上経過で滞留
     * - !isAfter を使うことで「7日ちょうど」を含む（isBefore だと7日ちょうどを含まない）
     *   - isBefore(threshold) → 「threshold より前」→ 7日ちょうどを含まない
     *   - !isAfter(threshold) → 「threshold と同じか前」→ 7日ちょうどを含む ★こちらが正しい
     *
     * @return true: 滞留している（7日以上未対応 かつ 未完了）
     */
    public boolean isStagnant() {
        // 完了済みなら滞留しない
        if ("RESOLVED".equals(status)) {
            return false;
        }

        // 一度も対応していない場合 → 受付日から7日以上で滞留
        if (lastContactedAt == null) {
            if (occurredAt == null) {
                return false;
            }
            LocalDate today = LocalDate.now();
            // occurredAt + 7日 が today 以前なら滞留
            return !today.isBefore(occurredAt.plusDays(7));
        }

        // 最終対応日から7日以上経過で滞留
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        return !lastContactedAt.isAfter(threshold);
    }
}
