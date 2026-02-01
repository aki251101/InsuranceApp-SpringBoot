package jp.yoshiaki.insuranceapp.training.day62.notes.domain;

import java.util.Locale;

/**
 * ノートの状態（内部ロジックは英語、表示は日本語ラベルへ変換）
 */
public enum NoteStatus {
    TODO("未着手"),
    DOING("進行中"),
    DONE("完了");

    private final String labelJa;

    NoteStatus(String labelJa) {
        this.labelJa = labelJa;
    }

    public String getLabelJa() {
        return labelJa;
    }

    /**
     * 入力文字列を状態に変換する。
     * - "TODO/DOING/DONE"（大文字小文字は吸収）
     * - "未着手/進行中/完了"
     */
    public static NoteStatus parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("status が空です");
        }
        String s = raw.trim();

        // 日本語ラベル
        for (NoteStatus st : values()) {
            if (st.labelJa.equals(s)) {
                return st;
            }
        }

        // 英語（大小無視）
        String upper = s.toUpperCase(Locale.ROOT);
        return NoteStatus.valueOf(upper);
    }
}
