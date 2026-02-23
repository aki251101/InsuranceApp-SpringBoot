package jp.yoshiaki.insuranceapp.training.day84.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 天気情報取得の業務サービス。
 * 外部API呼び出し → リトライ → ログ分類 → メトリクス記録 を一連で行う。
 *
 * 【ログレベルの使い分け基準】
 * - INFO   : 正常完了の記録（後から「いつ成功したか」を追える）
 * - WARNING: 想定内の失敗でリトライ中（注意だが即対応は不要）
 * - SEVERE : 全リトライ失敗（即対応が必要。運用チームへのアラート対象）
 *
 * ※java.util.loggingでは ERROR の代わりに SEVERE を使う。
 *   SLF4J/Logbackでは ERROR に対応する。
 */
public class WeatherService {

    // ① ロガー（このクラス専用。クラス名がログに出る）
    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    // ② 最大リトライ回数（つまみ：増やすと粘り強く、減らすと早期失敗）
    private static final int MAX_RETRIES = 3;

    // ③ 外部APIクライアント（interfaceで注入→差し替え可能）
    private final WeatherApiClient apiClient;

    // ④ メトリクスカウンター（成功/失敗/リトライを計測）
    private final MetricsCounter metrics;

    /**
     * コンストラクタ。
     * DIパターン：外部依存をコンストラクタで受け取る。
     *
     * @param apiClient 外部APIクライアント
     * @param metrics   メトリクスカウンター
     */
    public WeatherService(WeatherApiClient apiClient, MetricsCounter metrics) {
        this.apiClient = apiClient;
        this.metrics = metrics;

        // ⑤ ロガーの設定（INFO以上を出力、カスタムフォーマット）
        LoggerConfig.configure(logger, Level.INFO);
    }

    /**
     * 指定都市の天気を取得する。
     * 外部APIが失敗した場合はリトライし、ログとメトリクスを記録する。
     *
     * @param city 都市名
     * @return 天気情報の文字列
     * @throws ExternalServiceException 全リトライ失敗時
     */
    public String getWeather(String city) {
        RuntimeException lastException = null;

        // ⑥ リトライループ（最大MAX_RETRIES回試行）
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // 外部API呼び出し
                String result = apiClient.fetchWeather(city);

                // ⑦ リトライで成功した場合（attempt > 1）はWARNログ
                if (attempt > 1) {
                    logger.warning(String.format(
                            "外部API リトライ後に成功: city=%s, attempts=%d", city, attempt));
                    metrics.increment("retry_success");
                } else {
                    // ⑧ 1回目で成功した場合はINFOログ
                    logger.info(String.format(
                            "外部API呼び出し成功: city=%s", city));
                }

                // 成功カウントを記録
                metrics.increment("success");
                return result;

            } catch (RuntimeException e) {
                // ⑨ 失敗時：リトライカウントを記録し、WARNログ
                lastException = e;
                metrics.increment("retry");

                logger.warning(String.format(
                        "外部API リトライ中: city=%s, attempt=%d/%d, error=%s",
                        city, attempt, MAX_RETRIES, e.getMessage()));

                // リトライ間隔（実務では指数バックオフを使うが、今回は簡易版）
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(500); // 0.5秒待機
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // ⑩ 全リトライ失敗：ERRORログ＋失敗カウント＋業務例外をスロー
        logger.severe(String.format(
                "外部API 最終失敗: city=%s, attempts=%d, lastError=%s",
                city, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown"));

        metrics.increment("failure");

        // ⑪ 外部固有例外 → ExternalServiceExceptionに翻訳
        throw new ExternalServiceException("WeatherApi", MAX_RETRIES, lastException);
    }
}
