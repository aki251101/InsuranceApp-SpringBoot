package jp.yoshiaki.insuranceapp.training.day88.deploy;

/**
 * デプロイチェック項目の状態を表すenum。
 * OK=確認済み、NG=未確認。表示は日本語ラベルで行う。
 */
public enum CheckStatus {

    OK("確認済み"),
    NG("未確認");

    private final String label;

    CheckStatus(String label) {
        this.label = label;
    }

    /** 日本語ラベルを返す（一覧表示用） */
    public String getLabel() {
        return label;
    }
}
