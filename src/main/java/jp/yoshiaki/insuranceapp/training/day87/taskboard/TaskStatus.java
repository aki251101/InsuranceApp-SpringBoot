package jp.yoshiaki.insuranceapp.training.day87.taskboard;

/**
 * タスクの状態を表すenum。
 * 内部は英語（TODO/DONE）、表示は日本語（未着手/完了）で使い分ける。
 */
public enum TaskStatus {

    TODO("未着手"),
    DONE("完了");

    // ① 表示用の日本語ラベル
    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 日本語ラベルを返す（API レスポンスや画面表示用）。
     */
    public String getDisplayName() {
        return displayName;
    }
}
