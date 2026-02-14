package jp.yoshiaki.insuranceapp.training.day75.boundary;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WeatherClient の Fake（偽物）実装。
 * 開発・テスト用に固定値を返す。外部APIを一切呼ばない。
 *
 * @Profile("dev") の意味：
 * - Spring Boot の起動プロファイルが "dev" のときだけ、このクラスがBean登録される
 * - "prod" で起動した場合、このクラスは無視される（代わりに RealWeatherClient が使われる）
 *
 * Fakeのメリット：
 * 1. 外部APIのアカウント/APIキーが不要 → 環境構築なしで開発開始できる
 * 2. レスポンスが一定 → テスト結果が安定する（外部の状態に左右されない）
 * 3. レスポンスが即座 → 待ち時間ゼロで開発が高速に回る
 *
 * 損保アプリでの対応：
 * - FakeCalendarClient（Googleカレンダーの代わりに「登録しました」と返すだけ）
 * - FakeAiClient（Gemini AIの代わりに固定文を返すだけ）
 */
@Component("day75FakeWeatherClient")
@Profile("dev")
public class FakeWeatherClient implements WeatherClient {

    // ① 固定データ：都市名→天気情報のマッピング
    // 本番ではAPIから取得するが、Fakeでは事前に決めた値を返す
    private static final Map<String, WeatherInfo> FAKE_DATA = Map.of(
            "Tokyo",  new WeatherInfo("Tokyo",  WeatherCondition.SUNNY,  25),
            "Osaka",  new WeatherInfo("Osaka",  WeatherCondition.RAINY,  18),
            "Nagoya", new WeatherInfo("Nagoya", WeatherCondition.CLOUDY, 20),
            "Sapporo", new WeatherInfo("Sapporo", WeatherCondition.RAINY, 5)
    );

    /**
     * 固定データから天気情報を返す。
     * 登録されていない都市名の場合は CityNotFoundException をスローする。
     */
    @Override
    public WeatherInfo fetchWeather(String city) {
        // ② Fakeデータに存在するかチェック
        WeatherInfo info = FAKE_DATA.get(city);
        if (info == null) {
            // ③ 見つからない場合は業務例外をスロー
            throw new CityNotFoundException(city);
        }
        return info;
    }
}
