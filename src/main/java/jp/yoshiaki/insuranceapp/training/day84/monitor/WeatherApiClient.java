package jp.yoshiaki.insuranceapp.training.day84.monitor;

/**
 * 外部天気APIクライアントのインターフェース。
 * interfaceにすることで、本番実装とテスト用モックを差し替えられる。
 *
 * 実務では：
 * - 本番 → HttpWeatherApiClient（実際のHTTP通信）
 * - テスト → MockWeatherApiClient（固定値を返す）
 * - 今回 → SimulatedWeatherApiClient（ランダム失敗をシミュレート）
 */
public interface WeatherApiClient {

    /**
     * 指定した都市の天気情報を取得する。
     *
     * @param city 都市名（例："tokyo"）
     * @return 天気情報の文字列（例："晴れ / 18℃"）
     * @throws RuntimeException 外部API通信に失敗した場合
     */
    String fetchWeather(String city);
}
