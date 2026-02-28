package jp.yoshiaki.insuranceapp.training.day78.weather;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * WeatherClient の実装を Profile で切り替える設定クラス。
 *
 * Springの @Profile を使うと、application.yaml の
 * spring.profiles.active の値に応じて、
 * 「どの実装をBeanとして登録するか」を自動で切り替えられる。
 *
 * - dev（開発）  → FakeWeatherClient（外部APIを呼ばない）
 * - prod（本番） → RealWeatherClient（本物の外部APIを呼ぶ）
 *
 * ※ Bean名に "day78" 接頭辞を付けて他Dayとの衝突を防止
 *
 * 【重要：@Value と @Bean の関係】
 * RealWeatherClient は @Value で設定値を注入するため、
 * @Bean メソッド内で new すると @Value が効かない。
 * → RealWeatherClient には @Component を付けて Spring に生成を任せ、
 *   この Config では Spring が生成済みの Bean を受け取って返す。
 */
@Configuration("day78WeatherClientConfig")
public class WeatherClientConfig {

    /**
     * 開発環境用：Fake実装を返す。
     * spring.profiles.active=dev のとき、このBeanが有効になる。
     *
     * FakeWeatherClient は @Value を使わないので new でOK。
     */
    @Bean("day78WeatherClient")
    @Profile("dev")
    public WeatherClient fakeWeatherClient() {
        return new FakeWeatherClient();
    }

    /**
     * 本番環境用：Real実装を返す。
     * spring.profiles.active=prod のとき、このBeanが有効になる。
     *
     * 引数の RealWeatherClient は Spring が @Component で生成済み。
     * @Value の注入も完了している状態で受け取る。
     */
    @Bean("day78WeatherClient")
    @Profile("prod")
    public WeatherClient realWeatherClient(RealWeatherClient realClient) {
        return realClient;
    }
}
