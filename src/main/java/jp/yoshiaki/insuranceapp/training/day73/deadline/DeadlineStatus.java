package jp.yoshiaki.insuranceapp.training.day73.deadline;

/**
 * タスクの状態を表すenum（列挙型）
 *
 * OPEN  = 未完了（まだやっていない）
 * DONE  = 完了（終わった）
 */
public enum DeadlineStatus {

    OPEN("未完了"),
    DONE("完了");

    // ① 日本語ラベルを保持するフィールド
    private final String label;

    // ② コンストラクタ（enumの各値に日本語ラベルを紐づける）
    DeadlineStatus(String label) {
        this.label = label;
    }

    // ③ 日本語ラベルを返すメソッド（表示用）
    public String getLabel() {
        return label;
    }
}
