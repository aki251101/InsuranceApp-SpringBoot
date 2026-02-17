package jp.yoshiaki.insuranceapp.training.day78.weather;

/**
 * 天気情報を取得するクライアントの契約（Interface）。
 *
 * 実装を差し替え可能にすることで：
 * - 開発中は FakeWeatherClient（固定値を返す）
 * - 本番では RealWeatherClient（実際にAPIを呼ぶ）
 * をSpringのProfileで切り替えられる。
 */
public interface WeatherClient {

    /**
     * 指定した都市の天気情報を取得する。
     *
     * @param city 都市名（例："Tokyo"）
     * @return 天気情報のレスポンス
     * @throws ExternalApiException 外部API呼び出しに失敗した場合
     */
    WeatherResponse fetchWeather(String city);
}
