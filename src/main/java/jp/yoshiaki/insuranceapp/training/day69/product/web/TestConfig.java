package jp.yoshiaki.insuranceapp.training.day69.product.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * テスト専用の起動設定。
 * このクラスがあることで、テスト時にメインの InsuranceAppSpringBootApplication
 * （GreetingServiceを使うCommandLineRunnerが入っている）が読み込まれなくなる。
 */
@SpringBootApplication(scanBasePackages = "jp.yoshiaki.insuranceapp.training.day69.product")
public class TestConfig {
}