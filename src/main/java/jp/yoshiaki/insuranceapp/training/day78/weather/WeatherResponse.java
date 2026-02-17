package jp.yoshiaki.insuranceapp.training.day78.weather;

/**
 * 天気情報のレスポンスDTO。
 * 外部APIから取得した天気データを格納して返却する入れ物。
 */
public class WeatherResponse {

    /** 都市名（例："Tokyo"） */
    private final String city;

    /** 天気（例："晴れ"） */
    private final String weather;

    /** 気温（℃）（例：22.5） */
    private final double temperature;

    // ① 全フィールドをコンストラクタで初期化（不変にするため final + コンストラクタ）
    public WeatherResponse(String city, String weather, double temperature) {
        this.city = city;
        this.weather = weather;
        this.temperature = temperature;
    }

    public String getCity() {
        return city;
    }

    public String getWeather() {
        return weather;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return String.format("WeatherResponse{city='%s', weather='%s', temperature=%.1f℃}",
                city, weather, temperature);
    }
}
