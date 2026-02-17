package jp.yoshiaki.insuranceapp.training.day75.boundary;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 天気に関する業務ロジックを担当するサービス。
 *
 * ★ 境界Interfaceの恩恵を受ける側 ★
 *
 * このクラスは WeatherClient（interface）だけを知っている。
 * 「Fake か Real か」は一切知らない。
 *
 * コンストラクタインジェクションの流れ：
 * 1. Spring Boot 起動時に @Profile を見て、FakeかRealかを決定
 * 2. 決定した実装クラスのインスタンスを生成
 * 3. WeatherService のコンストラクタに「WeatherClient型」として渡す
 * → WeatherService は「渡されたものを使うだけ」（何が来たか気にしない）
 *
 * この設計が「疎結合」の正体：
 * - Service が Client の「具体的な実装」に依存しない
 * - Interface（約束）にだけ依存する
 * - だから実装を差し替えても Service のコードは1行も変わらない
 */
@Profile("training")
@Service("day75WeatherService")
public class WeatherService {

    private final WeatherClient weatherClient;  // interface型で受け取る（重要！）

    // ① コンストラクタインジェクション：Springが自動で実装を注入する
    // @Autowired は省略可能（コンストラクタが1つだけの場合）
    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    /**
     * 指定された都市の天気情報を取得する。
     *
     * このメソッドは weatherClient.fetchWeather() に委譲するだけ。
     * 「Fakeが返す固定値」も「Realが返すAPI結果」も、同じ WeatherInfo 型で受け取る。
     *
     * @param city 都市名
     * @return 天気情報
     * @throws CityNotFoundException 都市が見つからない場合
     * @throws ExternalApiException 外部API呼び出しに失敗した場合
     */
    public WeatherInfo getWeather(String city) {
        // ② interfaceのメソッドを呼ぶだけ → Fake/Realどちらが動くかはSpringが決める
        return weatherClient.fetchWeather(city);
    }
}
