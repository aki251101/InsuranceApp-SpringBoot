package jp.yoshiaki.insuranceapp.training.day81.notification;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring全体で非同期処理（@Async）を有効にする設定クラス。
 *
 * 【重要】@EnableAsync が無いと、@Async を付けたメソッドも「同期」で動いてしまう。
 * エラーにはならず、見かけ上は正常動作するため、気づきにくいバグになる。
 *
 * たとえ話：
 *   @Async ＝「この仕事は別の人にやってもらう」という指示書
 *   @EnableAsync ＝「別の人に仕事を頼める体制」を会社として整えるスイッチ
 *   → スイッチがOFFだと、指示書を書いても自分でやることになる
 */
@Configuration("day81AsyncConfig")
@EnableAsync
public class AsyncConfig {
    // このクラス自体にはメソッドは不要。
    // @EnableAsync アノテーションが付いていることが重要。
    // Springがこのクラスを見つけると、@Asyncの仕組みが有効になる。
}
