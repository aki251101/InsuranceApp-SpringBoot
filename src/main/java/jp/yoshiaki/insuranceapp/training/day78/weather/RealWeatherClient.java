package jp.yoshiaki.insuranceapp.training.day78.weather;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 本物の外部API呼び出しを行う天気クライアント。
 *
 * application.yml の設定値を @Value で読み込み、
 * RestTemplate で外部APIにGETリクエストを送る。
 *
 * 起動時に @PostConstruct で必須設定値を検証し、
 * 未定義なら即座にエラーを出してアプリを停止させる（フェイルファスト）。
 *
 * ※ Bean名は "day78RealWeatherClient" で一意化（他Dayとの衝突防止）
 * ※ @Profile("prod") を付けて、dev 環境では Bean 生成自体をスキップする
 *    → dev では @Value の設定値が yml に無くても起動エラーにならない
 */
@Component("day78RealWeatherClient")
@Profile("prod")
public class RealWeatherClient implements WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(RealWeatherClient.class);

    // ① @Value で application.yml の設定値をフィールドに注入する
    //    ${...} はプレースホルダ（yml のキーを指す）
    @Value("${weather.api.base-url}")
    private String baseUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    // ② RestTemplate：Springが提供するHTTP通信の道具
    //    外部APIにGET/POST等のリクエストを送れる
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Bean生成直後に1回だけ呼ばれるバリデーション。
     * 必須設定値が未定義の場合、起動時にわかりやすく失敗させる。
     *
     * なぜ起動時に検証するのか？
     * → リクエストが来てから「キーが無い」と気づくと、
     *   ユーザーに500エラーを返してしまう。
     *   起動時に落とせば「デプロイ前に気づける」ので安全。
     */
    @PostConstruct
    public void validateConfig() {
        // ③ null または空文字の場合は起動を止める
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "weather.api.base-url が未設定です。application.yml を確認してください。");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "weather.api.key が未設定です。application.yml を確認してください。");
        }
        log.info("天気API設定を検証OK: base-url={}", baseUrl);
    }

    @Override
    public WeatherResponse fetchWeather(String city) {
        // ④ URLを組み立てて外部APIにGETリクエストを送る
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=ja",
                baseUrl, city, apiKey);
        log.info("外部API呼び出し: city={}, url={}", city, maskUrl(url));

        try {
            // ⑤ getForObject：GETリクエストを送り、結果をMapで受け取る
            //    第1引数=URL、第2引数=レスポンスを変換する型
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new ExternalApiException("外部APIからの応答がnullです: city=" + city);
            }

            // ⑥ レスポンスJSONから必要な値を取り出してDTOに変換
            return parseResponse(city, response);

        } catch (RestClientException e) {
            // ⑦ 通信エラー（タイムアウト、接続拒否、404等）を業務例外に変換
            log.error("外部API呼び出し失敗: city={}", city, e);
            throw new ExternalApiException(
                    "天気情報の取得に失敗しました: city=" + city, e);
        }
    }

    /**
     * 外部APIのレスポンスJSONから天気情報を取り出す。
     * JSONの構造に依存するため、解析失敗時は業務例外をスローする。
     */
    @SuppressWarnings("unchecked")
    private WeatherResponse parseResponse(String city, Map<String, Object> response) {
        try {
            // JSONの構造例: {"weather":[{"description":"晴れ"}], "main":{"temp":22.5}}
            var weatherList = (java.util.List<Map<String, Object>>) response.get("weather");
            String weather = (weatherList != null && !weatherList.isEmpty())
                    ? (String) weatherList.get(0).get("description")
                    : "不明";

            var main = (Map<String, Object>) response.get("main");
            double temp = (main != null && main.get("temp") != null)
                    ? ((Number) main.get("temp")).doubleValue()
                    : 0.0;

            return new WeatherResponse(city, weather, temp);

        } catch (ClassCastException | NullPointerException e) {
            log.error("APIレスポンス解析失敗: city={}", city, e);
            throw new ExternalApiException(
                    "天気情報の解析に失敗しました: city=" + city, e);
        }
    }

    /**
     * ログに出すURLからAPIキーを隠す（セキュリティ対策）。
     */
    private String maskUrl(String url) {
        return url.replaceAll("appid=[^&]+", "appid=***");
    }
}
