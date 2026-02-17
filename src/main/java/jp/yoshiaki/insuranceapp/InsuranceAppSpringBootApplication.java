package jp.yoshiaki.insuranceapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

// ① training パッケージ配下をコンポーネントスキャンから除外する。
//    これにより training 内の @Component / @Service / @RestController 等は
//    Spring に Bean として登録されない（＝本体アプリに一切干渉しない）。
//
//    除外対象: jp.yoshiaki.insuranceapp.training 配下の全クラス
//    除外方法: REGEX（正規表現）でパッケージパスを指定
//
//    【注意】
//    - @SpringBootApplication には @ComponentScan が内蔵されているが、
//      明示的に @ComponentScan を書くと内蔵のものが上書きされる。
//      そのため basePackages に本体のルートパッケージを必ず指定すること。
//    - training の動作確認をしたい場合は excludeFilters をコメントアウトすれば
//      全 Day のミニアプリが再び起動する。
@SpringBootApplication
@EnableScheduling

// training 動作時は以下の @ComponentScan のブロックを丸ごと /* */ で囲んでコメントアウトする

@ComponentScan(
        basePackages = "jp.yoshiaki.insuranceapp",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "jp\\.yoshiaki\\.insuranceapp\\.training\\..*"
        )
)

public class InsuranceAppSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceAppSpringBootApplication.class, args);
    }
}