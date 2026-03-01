package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * /actuator/info エンドポイントにアプリ情報を追加する。
 *
 * InfoContributor インターフェースを実装すると、
 * Actuator が自動的にこのクラスを検出し、
 * /actuator/info のレスポンスに情報を組み込む。
 *
 * 【使いどころ】
 * - デプロイ後に「今動いているのはどのバージョンか」を確認する
 * - 運用担当者が「このアプリは何か」をAPIで確認できる
 */
@Component("day90AppInfoContributor")  // Bean名を明示（他Dayとの衝突防止）
public class AppInfoContributor implements InfoContributor {

    /**
     * /actuator/info のレスポンスに情報を追加する。
     *
     * @param builder 情報を追加するためのビルダー
     */
    @Override
    public void contribute(Info.Builder builder) {
        // ① アプリケーション情報を追加
        builder.withDetail("app", Map.of(
                "name", "BookStock管理アプリ（Day90学習版）",
                "version", "1.0.0",
                "description", "Spring Boot Actuator の学習用ミニアプリ"
        ));

        // ② 開発者情報を追加
        builder.withDetail("developer", Map.of(
                "name", "よちぞ〜",
                "challenge", "100日連続コーディングチャレンジ Day90"
        ));
    }
}
