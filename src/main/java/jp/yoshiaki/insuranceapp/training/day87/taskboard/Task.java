package jp.yoshiaki.insuranceapp.training.day87.taskboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * タスクを表すEntityクラス。
 * H2データベースの tasks テーブルに対応する。
 */
@Entity
@Table(name = "tasks")
public class Task {

    // ① 主キー（自動採番）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ② タスクのタイトル（必須）
    @Column(nullable = false)
    private String title;

    // ③ タスクの状態（TODO / DONE を文字列で保存）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    // ④ 作成日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA が使う引数なしコンストラクタ（必須）
    protected Task() {
    }

    /**
     * 新規タスクを作成するコンストラクタ。
     * 状態は TODO（未着手）、作成日時は現在時刻で初期化する。
     */
    public Task(String title) {
        this.title = title;
        this.status = TaskStatus.TODO;
        this.createdAt = LocalDateTime.now();
    }

    // --- getter ---

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- 業務メソッド ---

    /**
     * タスクを完了にする。
     * すでに完了している場合は何もしない（冪等）。
     */
    public void complete() {
        this.status = TaskStatus.DONE;
    }
}
