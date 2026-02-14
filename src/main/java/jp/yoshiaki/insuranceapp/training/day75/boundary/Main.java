package jp.yoshiaki.insuranceapp.training.day75.boundary;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * 天気通知ミニアプリのエントリポイント。
 * Spring Boot の CommandLineRunner を実装し、起動後にコンソール操作を開始する。
 *
 * CommandLineRunner とは：
 * - Spring Boot アプリケーション起動後に自動で run() メソッドが呼ばれる仕組み
 * - Web アプリでなくても Spring の DI を使いたいときに便利
 * - 今回は WeatherService を DI で受け取り、コンソールアプリとして動かす
 */
@Component("day75Main")
public class Main implements CommandLineRunner {

    private final WeatherService weatherService;

    // ① コンストラクタインジェクション：Spring が WeatherService を自動注入
    public Main(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 天気通知ミニアプリ（Day75：境界Interface学習） ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            // ② 空入力はスキップ
            if (line.isEmpty()) {
                continue;
            }

            // ③ コマンド解析：スペースで分割し、先頭をコマンドとして扱う
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "weather": {
                    // ④ 都市名の有無をチェック
                    if (parts.length < 2 || parts[1].isBlank()) {
                        System.out.println("エラー：都市名を指定してください（例：weather Tokyo）");
                        break;
                    }
                    String city = parts[1].trim();
                    handleWeather(city);
                    break;
                }
                case "help": {
                    printHelp();
                    break;
                }
                case "exit": {
                    System.out.println("アプリを終了します。");
                    return;
                }
                default: {
                    System.out.println("不明なコマンドです。helpで操作一覧を確認してください。");
                    break;
                }
            }
            System.out.println();
        }
    }

    /**
     * 天気取得コマンドの処理。
     * WeatherService に委譲し、結果またはエラーを表示する。
     */
    private void handleWeather(String city) {
        try {
            // ⑤ Service経由で天気を取得（Fake/Realどちらが動くかはSpringが決定済み）
            WeatherInfo info = weatherService.getWeather(city);
            System.out.println(info.toDisplayString());
        } catch (CityNotFoundException e) {
            // ⑥ 業務例外：都市が見つからない
            System.out.println("エラー：" + e.getMessage());
        } catch (ExternalApiException e) {
            // ⑦ 外部API例外：API呼び出し失敗
            System.out.println("外部APIエラー：" + e.getMessage());
        } catch (Exception e) {
            // ⑧ 想定外の例外：安全網
            System.out.println("予期しないエラーが発生しました：" + e.getMessage());
        }
    }

    /**
     * 操作一覧を表示する。
     */
    private void printHelp() {
        System.out.println("【操作一覧】");
        System.out.println("  weather <都市名>  … 天気情報を取得する（例：weather Tokyo）");
        System.out.println("  help              … この一覧を表示する");
        System.out.println("  exit              … アプリを終了する");
        System.out.println();
        System.out.println("【対応都市（Fakeモード）】");
        System.out.println("  Tokyo / Osaka / Nagoya / Sapporo");
    }
}
