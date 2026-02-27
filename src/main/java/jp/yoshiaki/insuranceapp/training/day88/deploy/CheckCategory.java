package jp.yoshiaki.insuranceapp.training.day88.deploy;

/**
 * デプロイチェック項目のカテゴリを表すenum。
 * SERVER=サーバー設定、NETWORK=ネットワーク設定、APP=アプリ設定。
 */
public enum CheckCategory {

    SERVER("サーバー設定"),
    NETWORK("ネットワーク設定"),
    APP("アプリ設定");

    private final String label;

    CheckCategory(String label) {
        this.label = label;
    }

    /** 日本語ラベルを返す */
    public String getLabel() {
        return label;
    }

    /**
     * 入力文字列からCheckCategoryを返す。
     * 英語（server/network/app）と日本語（サーバー設定/ネットワーク設定/アプリ設定）の両方に対応。
     *
     * @param input ユーザー入力文字列
     * @return 対応するCheckCategory
     * @throws IllegalArgumentException 不正な入力の場合
     */
    public static CheckCategory parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("カテゴリが空です。");
        }

        String trimmed = input.trim().toLowerCase();

        // ① 英語名での一致判定
        for (CheckCategory category : values()) {
            if (category.name().toLowerCase().equals(trimmed)) {
                return category;
            }
        }

        // ② 日本語ラベルでの一致判定
        for (CheckCategory category : values()) {
            if (category.label.equals(input.trim())) {
                return category;
            }
        }

        throw new IllegalArgumentException(
                "不明なカテゴリです: " + input + "（server/network/app または日本語名で入力してください）"
        );
    }
}
