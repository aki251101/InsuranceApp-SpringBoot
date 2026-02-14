package jp.yoshiaki.insuranceapp.training.day75.boundary;

/**
 * 天気情報を保持するドメインクラス。
 * 外部APIから取得した天気データを「アプリ内部の形」に変換して持つ。
 *
 * ポイント：外部APIのレスポンス形式（JSON等）に依存せず、
 * アプリが使いやすい形（city, condition, temperature）で保持する。
 * これにより、外部APIが変わっても、このクラスの利用側は影響を受けない。
 */
public class WeatherInfo {

    private final String city;                    // 都市名
    private final WeatherCondition condition;     // 天気状態（enum）
    private final int temperature;                // 気温（℃）

    // ① コンストラクタ：3つの値を受け取って保持する
    public WeatherInfo(String city, WeatherCondition condition, int temperature) {
        this.city = city;
        this.condition = condition;
        this.temperature = temperature;
    }

    public String getCity() {
        return city;
    }

    public WeatherCondition getCondition() {
        return condition;
    }

    public int getTemperature() {
        return temperature;
    }

    /**
     * 傘が必要かどうかを判定する。
     * 天気が RAINY（雨）なら true を返す。
     *
     * ポイント：判定ロジックをドメインに置くことで、
     * MainやServiceが「雨かどうか」を自分で判断しなくて済む。
     */
    public boolean needsUmbrella() {
        return condition == WeatherCondition.RAINY;
    }

    /**
     * コンソール表示用の文字列を生成する。
     * 例：「東京の天気：☀ 晴れ（25℃）/ 傘：不要」
     */
    public String toDisplayString() {
        String umbrellaStatus = needsUmbrella() ? "必要" : "不要";
        return String.format("%sの天気：%s（%d℃）/ 傘：%s",
                city,
                condition.toDisplayString(),
                temperature,
                umbrellaStatus);
    }
}
