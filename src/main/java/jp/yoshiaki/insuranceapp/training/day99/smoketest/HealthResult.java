package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ヘルスチェック結果
 * アプリ全体の健康状態と、各コンポーネント（DB/外部API等）の状態を保持する。
 *
 * status: "UP"（全て正常）or "DOWN"（1つでも異常あり）
 * components: コンポーネント名 → "UP" or "DOWN" のマップ
 */
public class HealthResult {

    private String status;
    private Map<String, String> components;

    public HealthResult() {
        this.components = new LinkedHashMap<>(); // ① 順序を保持するLinkedHashMap
    }

    // --- 状態を設定するメソッド ---

    /**
     * コンポーネントの状態を追加する
     * @param name コンポーネント名（例："db", "externalApi"）
     * @param up   正常ならtrue、異常ならfalse
     */
    public void addComponent(String name, boolean up) {
        components.put(name, up ? "UP" : "DOWN");
    }

    /**
     * 全体のステータスを計算して設定する
     * 1つでもDOWNがあればDOWN、全てUPならUP
     */
    public void calculateOverallStatus() {
        boolean allUp = components.values().stream()
                .allMatch("UP"::equals); // ② メソッド参照で全件チェック
        this.status = allUp ? "UP" : "DOWN";
    }

    // --- getter / setter ---

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getComponents() {
        return components;
    }

    public void setComponents(Map<String, String> components) {
        this.components = components;
    }
}
