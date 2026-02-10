package jp.yoshiaki.insuranceapp.training.day71.tasklog;

/**
 * タスク1件を表すドメインクラス。
 * IDとタイトルは作成時に決まり、状態は操作で変化する。
 */
public class Task {

    private final int id;
    private final String title;
    private TaskStatus status;

    public Task(int id, String title) {
        this.id = id;
        this.title = title;
        this.status = TaskStatus.TODO; // ① 初期状態は「未着手」
    }

    // --- getter ---
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    /** タスクを完了にする */
    public void done() {
        this.status = TaskStatus.DONE; // ② 状態をDONEに変更
    }

    /**
     * 表示用の文字列。状態は日本語ラベルで表示する。
     * 例: [1] 報告書作成（未着手）
     */
    @Override
    public String toString() {
        return String.format("[%d] %s（%s）", id, title, status.getDisplayName());
    }
}
