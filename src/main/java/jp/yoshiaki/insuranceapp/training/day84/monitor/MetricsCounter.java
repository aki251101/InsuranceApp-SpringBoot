package jp.yoshiaki.insuranceapp.training.day84.monitor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * メトリクス（指標）をカウントするクラス。
 * キーごとに「何回起きたか」を累積する。
 *
 * 実務では Micrometer / Prometheus 等の専用ライブラリを使うが、
 * 今回は「カウンターの概念」を理解するために自作する。
 *
 * たとえ話：来客数カウンター。ボタンを押すたびに1増える。減らない。
 */
public class MetricsCounter {

    // ① キー→カウント値のマップ（LinkedHashMapで挿入順を保持）
    private final Map<String, Integer> counters = new LinkedHashMap<>();

    /**
     * 指定キーのカウントを1増やす。
     * 初めてのキーなら0からスタートして1にする。
     *
     * @param key カウンター名（例："success", "failure", "retry"）
     */
    public void increment(String key) {
        // ② merge: キーが無ければ1をセット、あれば既存値に1を加算
        counters.merge(key, 1, Integer::sum);
    }

    /**
     * 指定キーの現在カウントを返す。
     * 存在しないキーは0を返す。
     *
     * @param key カウンター名
     * @return 現在のカウント値
     */
    public int get(String key) {
        return counters.getOrDefault(key, 0);
    }

    /**
     * 全カウンターの集計レポートを文字列で返す。
     * 運用ダッシュボードに表示するイメージ。
     *
     * @return レポート文字列
     */
    public String report() {
        if (counters.isEmpty()) {
            return "（まだ計測データがありません）";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===== メトリクス集計 =====\n");

        // ③ 各カウンターを「キー : 値」形式で表示
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            sb.append(String.format("  %-10s : %d 回%n", entry.getKey(), entry.getValue()));
        }

        // ④ 合計と成功率を計算して追記
        int success = get("success");
        int failure = get("failure");
        int total = success + failure;
        if (total > 0) {
            double successRate = (double) success / total * 100;
            sb.append(String.format("  ---------------------%n"));
            sb.append(String.format("  合計リクエスト : %d 回%n", total));
            sb.append(String.format("  成功率         : %.1f%%%n", successRate));
        }

        sb.append("==========================");
        return sb.toString();
    }

    /**
     * 全カウンターをリセットする（テスト用）。
     */
    public void reset() {
        counters.clear();
    }
}
