package jp.yoshiaki.insuranceapp.training.day78.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天気情報取得のREST APIエンドポイント。
 *
 * GET /api/day78/weather?city=Tokyo
 * → WeatherService に処理を委譲し、天気情報をJSONで返す。
 *
 * ※ Bean名は "day78WeatherController" で一意化（他Dayとの衝突防止）
 */
@RestController("day78WeatherController")
@RequestMapping("/api/day78/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    // ① コンストラクタインジェクション
    //    @Qualifier で Day78 の WeatherService を明示的に指定
    public WeatherController(
            @Qualifier("day78WeatherService") WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 天気情報を取得するAPIエンドポイント。
     *
     * @param city 都市名（クエリパラメータ、デフォルト："Tokyo"）
     * @return 天気情報のJSON
     *
     * 使い方（Postman）:
     *   GET http://localhost:8080/api/day78/weather?city=Tokyo
     */
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam(defaultValue = "Tokyo") String city) {

        log.info("天気情報APIリクエスト受信: city={}", city);

        // ② Service に委譲（Controller は入口の役割だけ）
        WeatherResponse response = weatherService.getWeather(city);

        return ResponseEntity.ok(response);
    }
}
