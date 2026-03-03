package jp.yoshiaki.insuranceapp.domain.policy;

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
 *
 * 【Day92で追加】
 * - renewalDueEndDate / renewedAt / cancelledAt フィールド
 * - getEffectiveStatus() / isRenewable() / isAttentionRequired() メソッド
 */
@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    // ① 主キー（自動採番）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ② 契約番号（業務上の一意キー。例：P-2026-0001）
    @Column(name = "policy_number", nullable = false, unique = true, length = 32)
    private String policyNumber;

    // ③ 契約者名
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    // ④ 契約開始日
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // ⑤ 契約終了日（満期日）
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // ⑥ 契約状態（DB保存値：ACTIVE / CANCELLED の2値）
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    // ⑦ 更新前満期日（更新取消時に元の満期日に戻すための退避用）
    @Column(name = "renewal_due_end_date")
    private LocalDate renewalDueEndDate;

    // ⑧ 更新操作日時（当日取消の判定に使用）
    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    // ⑨ 解約操作日時（当日取消の判定に使用）
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ⑩ Googleカレンダー連携用（Day93以降で使用予定）
    @Column(name = "calendar_event_id", length = 128)
    private String calendarEventId;

    @Column(name = "calendar_registered", nullable = false)
    private Boolean calendarRegistered = false;

    // ⑪ 作成日時（レコード初回保存時に自動設定）
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⑫ 更新日時（レコード更新のたびに自動設定）
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティ保存前に自動実行（JPA ライフサイクルコールバック）
     * 新規作成時に createdAt / updatedAt を自動設定する
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
     * エンティティ更新前に自動実行
     * 更新のたびに updatedAt を現在日時に上書きする
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 【Day92追加】実効ステータスを計算（画面表示用）
     *
     * DBには ACTIVE / CANCELLED の2値しか保存しないが、
     * 画面には「契約中」「解約」「失効」の3状態を表示したい。
     * このギャップを埋めるのがこのメソッド。
     *
     * 判定ロジック：
     *   1. status == "CANCELLED" → "解約"
     *   2. endDate < 今日 → "失効"（DB上ACTIVEだが期限切れ）
     *   3. それ以外 → "契約中"
     *
     * @return "契約中" / "解約" / "失効"
     */
    public String getEffectiveStatus() {
        // まず解約チェック（DBの状態が最優先）
        if ("CANCELLED".equals(status)) {
            return "解約";
        }
        // 次に失効チェック（ACTIVEだが満期日を過ぎている）
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            return "失効";
        }
        // どちらでもなければ契約中
        return "契約中";
    }

    /**
     * 【Day92追加】更新可能期間内かどうか判定
     *
     * 損保の実務では、満期日の2ヶ月前から更新手続きが可能。
     * 満期日を過ぎると更新不可（失効扱い）。
     *
     * 判定条件：
     *   - ステータスが ACTIVE であること
     *   - 今日が「満期2ヶ月前 ≦ 今日 ≦ 満期日」の範囲内であること
     *
     * @return true: 更新可能
     */
    public boolean isRenewable() {
        if (!"ACTIVE".equals(status) || endDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate renewableStart = endDate.minusMonths(2);
        // renewableStart <= today <= endDate
        return !today.isBefore(renewableStart) && !today.isAfter(endDate);
    }

    /**
     * 【Day92追加】要注意期間内かどうか判定
     *
     * 満期日まで20日を切った契約を「要注意」として
     * 画面上で色を変えるなどの表示に使用する。
     *
     * @return true: 要注意（満期20日前〜満期日）
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
