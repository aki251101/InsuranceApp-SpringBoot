package jp.yoshiaki.insuranceapp.training.day78.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 天気情報取得の業務サービス。
 *
 * WeatherClient（Interface）を通じて天気データを取得する。
 * Service は「どの実装が動いているか」を知らない。
 * → Fake でも Real でも、このクラスのコードは一切変わらない。
 *
 * ※ Bean名は "day78WeatherService" で一意化（他Dayとの衝突防止）
 * ※ @Qualifier で注入する Bean 名を明示（同じ型の Bean が複数ある場合の指定）
 */
@Service("day78WeatherService")
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherClient weatherClient;

    // ① コンストラクタインジェクション
    //    @Qualifier で「day78WeatherClient」という名前の Bean を注入
    //    → Profile に応じて Fake or Real が入る
    public WeatherService(@Qualifier("day78WeatherClient") WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    /**
     * 指定した都市の天気情報を取得する。
     *
     * @param city 都市名（例："Tokyo"）
     * @return 天気情報のレスポンスDTO
     * @throws ExternalApiException 外部API呼び出しに失敗した場合
     */
    public WeatherResponse getWeather(String city) {
        log.info("天気情報を取得します: city={}", city);

        // ② WeatherClient に委譲（Fake か Real かはここでは気にしない）
        WeatherResponse response = weatherClient.fetchWeather(city);

        log.info("天気情報の取得に成功しました: {}", response);
        return response;
    }
}
