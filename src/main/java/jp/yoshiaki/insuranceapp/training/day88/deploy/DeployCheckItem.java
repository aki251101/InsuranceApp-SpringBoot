package jp.yoshiaki.insuranceapp.training.day88.deploy;

/**
 * デプロイ前のチェック項目1件を表すドメインクラス。
 * 各項目にはID、項目名、カテゴリ、状態（OK/NG）がある。
 */
public class DeployCheckItem {

    private final int id;
    private final String name;
    private final CheckCategory category;
    private CheckStatus status;  // 状態は変更可能

    public DeployCheckItem(int id, String name, CheckCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.status = CheckStatus.NG;  // 初期状態は「未確認」
    }

    // --- 状態変更メソッド ---

    /** 確認済み（OK）にする */
    public void markOk() {
        this.status = CheckStatus.OK;
    }

    /** 未確認（NG）にリセットする */
    public void markNg() {
        this.status = CheckStatus.NG;
    }

    /** 確認済みかどうかを返す */
    public boolean isOk() {
        return this.status == CheckStatus.OK;
    }

    // --- getter ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CheckCategory getCategory() {
        return category;
    }

    public CheckStatus getStatus() {
        return status;
    }

    /**
     * 一覧表示用の文字列を返す。
     * 例: "[1] [未確認] [サーバー設定] EC2/Lightsailインスタンス作成"
     */
    @Override
    public String toString() {
        return String.format("[%d] [%s] [%s] %s",
                id,
                status.getLabel(),
                category.getLabel(),
                name);
    }
}
