package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Day90 学習版の起動クラス。
 *
 * @SpringBootApplication は以下の3つを兼ねるアノテーション：
 *   - @Configuration     : このクラスが設定クラスであることを示す
 *   - @EnableAutoConfiguration : Spring Boot の自動設定を有効にする
 *   - @ComponentScan     : このパッケージ以下のBeanを自動検出する
 *
 * scanBasePackages で day90.health に限定し、他Dayのクラスを拾わないようにする。
 */
@SpringBootApplication(scanBasePackages = "jp.yoshiaki.insuranceapp.training.day90.health")
public class Day90Main {

    public static void main(String[] args) {
        SpringApplication.run(Day90Main.class, args);
        System.out.println();
        System.out.println("==============================================");
        System.out.println("  BookStock管理アプリ（Day90）が起動しました");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("【業務API】");
        System.out.println("  POST http://localhost:8080/api/day90/books   → 書籍登録");
        System.out.println("  GET  http://localhost:8080/api/day90/books   → 書籍一覧");
        System.out.println();
        System.out.println("【Actuator エンドポイント】");
        System.out.println("  GET  http://localhost:8080/actuator/health   → ヘルスチェック");
        System.out.println("  GET  http://localhost:8080/actuator/info     → アプリ情報");
        System.out.println();
    }
}
