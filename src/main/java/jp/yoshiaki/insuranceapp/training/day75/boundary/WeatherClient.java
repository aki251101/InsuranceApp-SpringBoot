package jp.yoshiaki.insuranceapp.training.day75.boundary;

/**
 * 外部天気APIとの境界（バウンダリ）を定義するインターフェース。
 *
 * ★ 今日の学習の核心 ★
 *
 * このinterfaceが「境界Interface（バウンダリインターフェース）」です。
 *
 * 役割：
 * - 「天気を取得する」という"約束（契約）"だけを定義する
 * - 「どうやって取得するか（HTTP? ファイル? 固定値?）」は書かない
 * - 実装クラス（FakeWeatherClient / RealWeatherClient）が「やり方」を決める
 *
 * メリット：
 * 1. WeatherService は「WeatherClient」だけを知っていれば良い
 *    → Fake か Real かを知らなくて良い（疎結合）
 * 2. テスト時は Fake、本番は Real に差し替えるだけ
 *    → コードの変更なしに切り替え可能（@Profile で制御）
 * 3. 外部APIの仕様変更が影響するのは Real 実装だけ
 *    → Service や Main は影響を受けない
 *
 * 損保アプリでの対応：
 * - CalendarClient（Googleカレンダー連携の窓口）
 * - AiClient（Gemini AI連携の窓口）
 * と同じ設計パターンになる。
 */
public interface WeatherClient {

    /**
     * 指定された都市の天気情報を取得する。
     *
     * @param city 都市名（例："Tokyo", "Osaka"）
     * @return 天気情報（WeatherInfo）
     * @throws CityNotFoundException 都市が見つからない場合
     * @throws ExternalApiException 外部API呼び出しに失敗した場合
     */
    WeatherInfo fetchWeather(String city);
}
