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
 * accidents テーブルに対応
 */
@Entity
@Table(name = "accidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accident {

    /**
     * 事故ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 契約ID（外部キー）
     */
    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    /**
     * 契約情報（Policyエンティティとの関連）
     * FetchType.LAZY: 必要な時だけ読み込む
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private Policy policy;

    /**
     * 事故受付日
     */
    @Column(name = "occurred_at", nullable = false)
    private LocalDate occurredAt;

    /**
     * 事故場所
     */
    @Column(name = "place", length = 200)
    private String place;

    /**
     * 事故概要
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * 事故状態: OPEN, IN_PROGRESS, RESOLVED
     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 最終対応日時（滞留判定用）
     */
    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    /**
     * 対応履歴メモ
     */
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    /**
     * 作成日時
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティ保存前に自動実行（作成日時設定）
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
     * エンティティ更新前に自動実行（更新日時設定）
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 事故ステータスの日本語表示
     *
     * @return 受付 / 対応中 / 完了
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
     * 滞留しているかどうか判定
     * 最終対応日から7日以上経過、かつ未完了
     *
     * 【修正】境界条件を修正
     *   旧: isBefore（7日ちょうどを含まない = 厳密に7日超過のみ滞留）
     *   新: !isAfter（7日ちょうどを含む = 7日以上で滞留）
     *   設計書: 「last_contacted_at <= today - 7日」= 7日以上経過で滞留
     *
     * @return true: 滞留
     */
    public boolean isStagnant() {
        if ("RESOLVED".equals(status)) {
            return false;
        }
        if (lastContactedAt == null) {
            // 一度も対応していない場合は受付日から判定
            // 【修正】7日ちょうどを含む判定に変更
            if (occurredAt == null) {
                return false;
            }
            LocalDate today = LocalDate.now();
            return !today.isBefore(occurredAt.plusDays(7));
        }
        // 【修正】7日ちょうどを含む判定に変更
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        return !lastContactedAt.isAfter(threshold);
    }
}
