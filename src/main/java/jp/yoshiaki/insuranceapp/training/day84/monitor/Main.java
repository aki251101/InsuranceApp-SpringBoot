package jp.yoshiaki.insuranceapp.training.day84.monitor;

import java.util.Scanner;

/**
 * 天気情報取得サービスのCLIエントリーポイント。
 *
 * コマンド一覧：
 *   weather <都市名>  : 天気情報を取得（外部API呼び出し＋リトライ）
 *   stats             : メトリクス（成功/失敗/リトライ回数）の集計を表示
 *   help              : コマンド一覧を表示
 *   exit              : アプリを終了
 */
public class Main {

    public static void main(String[] args) {
        // ① 依存オブジェクトの組み立て（実務ではDIコンテナが担う部分）
        MetricsCounter metrics = new MetricsCounter();
        // 失敗率50%でシミュレート（0.5 = 2回に1回は失敗する設定）
        WeatherApiClient apiClient = new SimulatedWeatherApiClient(0.5);
        WeatherService service = new WeatherService(apiClient, metrics);

        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("  天気情報取得サービス（監視付き）");
        System.out.println("  外部API失敗のログ分類＋メトリクス計測");
        System.out.println("========================================");
        System.out.println();
        printHelp();
        System.out.println();

        // ② メインループ
        while (true) {
            System.out.print("操作を入力してください > ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // ③ コマンド解釈
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "weather": {
                    // ④ 天気取得コマンド
                    if (parts.length < 2 || parts[1].isBlank()) {
                        System.out.println("[エラー] 都市名を指定してください。例: weather tokyo");
                        break;
                    }
                    String city = parts[1].trim();
                    handleWeather(service, city);
                    break;
                }

                case "stats": {
                    // ⑤ メトリクス表示コマンド
                    System.out.println();
                    System.out.println(metrics.report());
                    System.out.println();
                    break;
                }

                case "help": {
                    printHelp();
                    break;
                }

                case "exit": {
                    System.out.println("アプリを終了します。お疲れさまでした！");
                    scanner.close();
                    return;
                }

                default: {
                    System.out.println("[エラー] 不明なコマンドです。helpでコマンド一覧を確認してください。");
                    break;
                }
            }
        }
    }

    /**
     * 天気取得を実行し、結果またはエラーメッセージを表示する。
     * ユーザーへの応答と運用ログは分離されている（ログはWeatherServiceが出力）。
     */
    private static void handleWeather(WeatherService service, String city) {
        System.out.println();
        System.out.println("天気情報を取得しています... （都市: " + city + "）");
        System.out.println("（※バックグラウンドでリトライ＋ログ出力が行われます）");
        System.out.println();

        try {
            // ⑥ 成功時：天気情報を表示
            String weather = service.getWeather(city);
            System.out.println("──────────────────────────");
            System.out.println("  都市: " + city);
            System.out.println("  天気: " + weather);
            System.out.println("──────────────────────────");
        } catch (ExternalServiceException e) {
            // ⑦ 最終失敗時：ユーザーには簡潔なメッセージを表示
            //    （詳細は運用ログに既に記録済み）
            System.out.println("──────────────────────────");
            System.out.println("  [失敗] 天気情報を取得できませんでした。");
            System.out.println("  サービス: " + e.getServiceName());
            System.out.println("  試行回数: " + e.getAttemptCount() + " 回");
            System.out.println("  ※ しばらく時間をおいて再度お試しください。");
            System.out.println("──────────────────────────");
        }
        System.out.println();
    }

    /**
     * コマンド一覧を表示する。
     */
    private static void printHelp() {
        System.out.println("【コマンド一覧】");
        System.out.println("  weather <都市名>  : 天気情報を取得（例: weather tokyo）");
        System.out.println("  stats             : メトリクス集計を表示（成功/失敗/リトライ回数）");
        System.out.println("  help              : このヘルプを表示");
        System.out.println("  exit              : アプリを終了");
        System.out.println();
        System.out.println("【登録済みの都市】tokyo, osaka, nagoya, fukuoka, sapporo");
    }
}
