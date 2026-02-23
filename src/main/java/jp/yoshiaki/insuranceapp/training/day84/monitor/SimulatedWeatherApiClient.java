package jp.yoshiaki.insuranceapp.training.day84.monitor;

import java.util.Map;
import java.util.Random;

/**
 * WeatherApiClientのシミュレーション実装。
 * 約50%の確率で外部API失敗（タイムアウト/接続エラー）を再現する。
 * 学習用として「外部失敗は日常的に起きる」ことを体験するためのクラス。
 */
public class SimulatedWeatherApiClient implements WeatherApiClient {

    // ① 都市名→天気のダミーデータ（本番ではHTTP通信で取得する部分）
    private static final Map<String, String> WEATHER_DATA = Map.of(
            "tokyo", "晴れ / 18℃",
            "osaka", "曇り / 15℃",
            "nagoya", "雨 / 12℃",
            "fukuoka", "晴れ / 20℃",
            "sapporo", "雪 / -2℃"
    );

    // ② 失敗確率を制御する乱数生成器
    private final Random random = new Random();

    // ③ 失敗確率（0.0〜1.0）。0.5 = 50%の確率で失敗
    private final double failureRate;

    /**
     * コンストラクタ。
     *
     * @param failureRate 失敗確率（0.0〜1.0。例：0.5なら50%で失敗）
     */
    public SimulatedWeatherApiClient(double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public String fetchWeather(String city) {
        // ④ 乱数が失敗確率未満なら、外部エラーをシミュレート
        if (random.nextDouble() < failureRate) {
            // 実務では ConnectException, SocketTimeoutException 等が飛ぶ
            throw new RuntimeException("外部API通信エラー: Connection timed out (simulated)");
        }

        // ⑤ 都市名に対応するデータがあれば返す、なければ未登録エラー
        String weather = WEATHER_DATA.get(city.toLowerCase());
        if (weather == null) {
            return "データなし（未登録の都市: " + city + "）";
        }
        return weather;
    }
}
