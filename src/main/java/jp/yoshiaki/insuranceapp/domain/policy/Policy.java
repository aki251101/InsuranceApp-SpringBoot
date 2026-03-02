// 配置：src/main/java/jp/yoshiaki/insuranceapp/domain/policy/Policy.java
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
 * ER図v1.1 の policies テーブルに1対1で対応する。
 * customer_name は Policy に直接保持する（MVP方針：Customer テーブルは分離しない）。
 */
@Entity                         // ① JPAに「このクラスはDBテーブルに対応する」と教える
@Table(name = "policies")       // ② 対応するテーブル名を明示（省略するとクラス名がテーブル名になる）
@Data                           // ③ Lombok：getter/setter/toString/equals/hashCode を自動生成
@NoArgsConstructor              // ④ Lombok：引数なしコンストラクタ（JPAが内部で使う）
@AllArgsConstructor             // ⑤ Lombok：全フィールド引数コンストラクタ
@Builder                        // ⑥ Lombok：Builder パターンでオブジェクトを組み立てられる
public class Policy {

    // ── 主キー ──────────────────────────────
    /** 契約ID（DB側の自動採番に任せる） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 業務カラム ──────────────────────────────

    /** 契約番号（業務上のユニークキー。例：P-2026-0001） */
    @Column(name = "policy_number", nullable = false, unique = true, length = 32)
    private String policyNumber;

    /** 契約者名（MVP では Customer テーブルを分けず、ここに保持する） */
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    /** 契約開始日 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 契約終了日（満期日）。作成時は startDate + 1年 で自動計算する */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** 契約状態: ACTIVE（有効）/ CANCELLED（解約） */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    // ── 更新/解約 関連 ──────────────────────────────

    /** 更新前の満期日（更新操作時に退避。早期更改率の集計で使う） */
    @Column(name = "renewal_due_end_date")
    private LocalDate renewalDueEndDate;

    /** 更新ボタン押下日時（早期更改判定に使用） */
    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    /** 解約ボタン押下日時 */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ── 外部連携（カレンダー）──────────────────────────────

    /** Google カレンダー側のイベントID（削除/更新用） */
    @Column(name = "calendar_event_id", length = 128)
    private String calendarEventId;

    /** カレンダー登録状態（true = 登録済み） */
    @Column(name = "calendar_registered", nullable = false)
    private Boolean calendarRegistered = false;

    // ── タイムスタンプ ──────────────────────────────

    /** 作成日時（INSERT時に自動設定。以降変更しない） */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時（INSERT・UPDATE時に自動設定） */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── ライフサイクルコールバック ──────────────────────────────

    /**
     * DB に初めて INSERT する直前に自動実行される。
     * created_at / updated_at を現在日時で埋め、
     * 未設定のフィールドにデフォルト値を入れる。
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
     * DB の既存行を UPDATE する直前に自動実行される。
     * updated_at を現在日時で上書きする。
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── 表示用・判定用メソッド ──────────────────────────────

    /**
     * 画面表示用の「実効ステータス」を返す。
     * DB上の status は ACTIVE / CANCELLED の2値だが、
     * 満期日を過ぎた ACTIVE は「失効」として扱う。
     *
     * @return "契約中" / "解約" / "失効"
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
     * 更新可能期間（満期2ヶ月前〜満期日）内かどうかを判定する。
     *
     * @return true：更新ボタンを押せる状態
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
     * 要注意期間（満期20日前〜満期日）内かどうかを判定する。
     *
     * @return true：画面上で注意表示をする
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
