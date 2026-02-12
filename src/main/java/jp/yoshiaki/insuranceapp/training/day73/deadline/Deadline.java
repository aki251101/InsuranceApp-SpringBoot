package jp.yoshiaki.insuranceapp.training.day73.deadline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 締め切りタスクを表すドメインクラス
 *
 * 1つのタスク（やるべきこと）を表す。
 * 「期限切れかどうか」「期限が近いかどうか」の判定ロジックを持つ。
 */
public class Deadline {

    // ① タスクID（一意な識別子）
    private Long id;

    // ② タスクのタイトル（例：「報告書提出」）
    private String title;

    // ③ 締め切り日（この日までにやる）
    private LocalDate dueDate;

    // ④ 状態（OPEN=未完了、DONE=完了）
    private DeadlineStatus status;

    // ⑤ 作成日時（いつ登録されたか）
    private LocalDateTime createdAt;

    /**
     * コンストラクタ（新規作成用）
     * IDはRepository側で採番するので、ここでは受け取らない
     */
    public Deadline(String title, LocalDate dueDate) {
        this.title = title;
        this.dueDate = dueDate;
        this.status = DeadlineStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * コンストラクタ（全フィールド指定：Repository復元用）
     */
    public Deadline(Long id, String title, LocalDate dueDate,
                    DeadlineStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    // === 判定メソッド（ビジネスロジック） ===

    /**
     * 期限切れかどうか判定する
     *
     * 条件：未完了（OPEN）かつ、締め切り日が今日より前
     * 例：締め切り=2/10、今日=2/12 → true（期限切れ）
     *
     * @return true: 期限切れ
     */
    public boolean isOverdue() {
        return status == DeadlineStatus.OPEN
                && dueDate.isBefore(LocalDate.now());
    }

    /**
     * 期限間近かどうか判定する
     *
     * 条件：未完了（OPEN）かつ、締め切り日が今日〜3日後以内
     * 例：締め切り=2/14、今日=2/12 → true（あと2日）
     *
     * @return true: 期限間近（3日以内）
     */
    public boolean isDueSoon() {
        if (status != DeadlineStatus.OPEN) {
            return false;
        }
        LocalDate today = LocalDate.now();
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
        // 0日（今日）〜3日後まで = 期限間近
        return daysUntilDue >= 0 && daysUntilDue <= 3;
    }

    /**
     * タスクを完了にする
     *
     * すでに完了の場合は何もしない（二重完了を防ぐ）
     */
    public void complete() {
        if (this.status == DeadlineStatus.DONE) {
            return;
        }
        this.status = DeadlineStatus.DONE;
    }

    /**
     * 締め切りまでの残り日数を返す（表示用）
     *
     * @return 残り日数（負の値は期限切れ日数）
     */
    public long daysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    // === Getter / Setter ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public DeadlineStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s（期限: %s / 状態: %s）",
                id, title, dueDate, status.getLabel());
    }
}
