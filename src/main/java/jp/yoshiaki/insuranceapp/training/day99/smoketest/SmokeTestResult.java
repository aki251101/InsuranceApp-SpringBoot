package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * スモークテスト一括実行の結果
 * 複数のテストケースをまとめて実行し、全体結果を保持する。
 *
 * overallResult: "ALL_PASS"（全テスト成功）or "HAS_FAILURE"（1つ以上失敗）
 * results: 各テストケースの結果リスト
 * executedAt: テスト実行日時
 */
public class SmokeTestResult {

    private String overallResult;
    private List<TestCaseResult> results;
    private String executedAt;

    public SmokeTestResult() {
        this.results = new ArrayList<>();
        this.executedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // ① 実行時刻を記録
    }

    /**
     * テストケースの結果を追加する
     * @param name   テスト名（例："ヘルスチェック", "商品一覧取得"）
     * @param passed 成功ならtrue
     * @param detail 詳細メッセージ
     */
    public void addResult(String name, boolean passed, String detail) {
        results.add(new TestCaseResult(name, passed ? "PASS" : "FAIL", detail));
    }

    /**
     * 全体結果を計算する
     * 1つでもFAILがあればHAS_FAILURE
     */
    public void calculateOverall() {
        boolean allPass = results.stream()
                .allMatch(r -> "PASS".equals(r.getResult())); // ② 全件PASSかチェック
        this.overallResult = allPass ? "ALL_PASS" : "HAS_FAILURE";
    }

    // --- getter / setter ---

    public String getOverallResult() {
        return overallResult;
    }

    public void setOverallResult(String overallResult) {
        this.overallResult = overallResult;
    }

    public List<TestCaseResult> getResults() {
        return results;
    }

    public void setResults(List<TestCaseResult> results) {
        this.results = results;
    }

    public String getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(String executedAt) {
        this.executedAt = executedAt;
    }

    // --- 内部クラス：テストケースの結果 ---

    /**
     * 個別テストケースの結果
     */
    public static class TestCaseResult {

        private String name;
        private String result;  // "PASS" or "FAIL"
        private String detail;

        public TestCaseResult(String name, String result, String detail) {
            this.name = name;
            this.result = result;
            this.detail = detail;
        }

        public String getName() {
            return name;
        }

        public String getResult() {
            return result;
        }

        public String getDetail() {
            return detail;
        }
    }
}
