package jp.yoshiaki.insuranceapp.training.day71.tasklog;

/**
 * タスクの状態を管理するenum。
 * 内部は英語（TODO/DOING/DONE）、表示は日本語ラベルに変換する。
 */
public enum TaskStatus {

    TODO("未着手"),
    DOING("進行中"),
    DONE("完了");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    /** 日本語ラベルを返す（表示用） */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 文字列からTaskStatusに変換する。
     * 英語（"done"）でも日本語（"完了"）でも受け付ける。
     *
     * @param input ユーザー入力
     * @return 対応するTaskStatus
     * @throws IllegalArgumentException 該当する状態がない場合
     */
    public static TaskStatus parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("状態の指定が空です");
        }
        String trimmed = input.trim();

        // 英語名で一致を試みる（大文字小文字を無視）
        for (TaskStatus status : values()) {
            if (status.name().equalsIgnoreCase(trimmed)) {
                return status;
            }
        }
        // 日本語ラベルで一致を試みる
        for (TaskStatus status : values()) {
            if (status.displayName.equals(trimmed)) {
                return status;
            }
        }
        throw new IllegalArgumentException("不明な状態です: " + trimmed);
    }
}
