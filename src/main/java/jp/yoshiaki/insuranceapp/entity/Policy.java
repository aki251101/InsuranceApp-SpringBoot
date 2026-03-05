package jp.yoshiaki.insuranceapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 契約エンティティ
 * policies テーブルに対応
 */
@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    /**
     * 契約ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 契約番号（業務ユニーク）
     */
    @Column(name = "policy_number", nullable = false, unique = true, length = 32)
    private String policyNumber;

    /**
     * 契約者名
     */
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    /**
     * 契約開始日
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 契約終了日（満期日）
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 契約状態: ACTIVE, CANCELLED
     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 更新前満期日（集計用）
     */
    @Column(name = "renewal_due_end_date")
    private LocalDate renewalDueEndDate;

    /**
     * 更新操作日時
     */
    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    /**
     * 解約操作日時
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Googleカレンダーイベント ID
     */
    @Column(name = "calendar_event_id", length = 128)
    private String calendarEventId;

    /**
     * カレンダー登録状態
     */
    @Column(name = "calendar_registered", nullable = false)
    private Boolean calendarRegistered = false;

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
        if (calendarRegistered == null) {
            calendarRegistered = false;
        }
        if (status == null) {
            status = "ACTIVE";
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
     * 実効ステータスを計算（画面表示用）
     *
     * @return 契約中 / 解約 / 失効
     */
    public String getEffectiveStatus() {
        if ("CANCELLED".equals(status)) {
            return "解約";
        }
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            return "失効";
        }
        return "契約中";
    }

    /**
     * 更新可能期間内かどうか判定
     * 満期2ヶ月前〜満期日
     *
     * @return true: 更新可能
     */
    public boolean isRenewable() {
        if (!"ACTIVE".equals(status) || endDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate renewableStart = endDate.minusMonths(2);
        return !today.isBefore(renewableStart) && !today.isAfter(endDate);
    }

    /**
     * 要注意期間内かどうか判定
     * 満期20日前〜満期日
     *
     * @return true: 要注意
     */
    public boolean isAttentionRequired() {
        if (endDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate attentionStart = endDate.minusDays(20);
        return !today.isBefore(attentionStart) && !today.isAfter(endDate);
    }
}
