package jp.yoshiaki.insuranceapp.training.day75.boundary;

/**
 * 天気の状態を表すenum。
 * 表示用の日本語ラベルとアイコンを持つ。
 */
public enum WeatherCondition {

    SUNNY("晴れ", "☀"),
    CLOUDY("曇り", "☁"),
    RAINY("雨", "☂");

    private final String label;  // 日本語表示用ラベル
    private final String icon;   // 表示用アイコン（絵文字）

    // ① コンストラクタ：enumの各値にラベルとアイコンを紐づける
    WeatherCondition(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * 表示用の文字列を返す（例：「☀ 晴れ」）
     */
    public String toDisplayString() {
        return icon + " " + label;
    }
}
