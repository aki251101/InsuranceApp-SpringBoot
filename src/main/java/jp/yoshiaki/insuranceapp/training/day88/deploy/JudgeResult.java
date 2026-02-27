package jp.yoshiaki.insuranceapp.training.day88.deploy;

import java.util.List;

/**
 * デプロイ判定の結果を保持するクラス。
 * 全項目がOKなら公開可能、1つでもNGなら公開不可（NG項目リスト付き）。
 */
public class JudgeResult {

    private final boolean deployable;              // 公開可能かどうか
    private final List<DeployCheckItem> ngItems;   // NG（未確認）の項目リスト

    public JudgeResult(boolean deployable, List<DeployCheckItem> ngItems) {
        this.deployable = deployable;
        this.ngItems = ngItems;
    }

    /** 公開可能かどうかを返す */
    public boolean isDeployable() {
        return deployable;
    }

    /** NG項目のリストを返す */
    public List<DeployCheckItem> getNgItems() {
        return ngItems;
    }
}
