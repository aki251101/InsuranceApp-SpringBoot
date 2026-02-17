package jp.yoshiaki.insuranceapp.training.day78.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * テスト用のダミー天気クライアント。
 * 外部APIを呼ばず、固定値を返す。
 *
 * 用途：
 * - 開発中に外部APIの契約・キーが無くても動作確認できる
 * - テスト時にネットワーク依存を排除できる
 *
 * ※ Bean名は "day78FakeWeatherClient" で一意化（他Dayとの衝突防止）
 * ※ @Profile("dev") は WeatherClientConfig で制御するため、ここには付けない
 */
public class FakeWeatherClient implements WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(FakeWeatherClient.class);

    @Override
    public WeatherResponse fetchWeather(String city) {
        // ① 外部APIを呼ばず、固定のダミーデータを返す
        log.info("[Fake] 天気情報を返却（ダミー）: city={}", city);
        return new WeatherResponse(city, "晴れ（ダミー）", 25.0);
    }
}
