package jp.yoshiaki.insuranceapp.training.day85.secret;

import java.util.Scanner;

/**
 * Day85 通知サービス - 秘密情報管理ミニアプリ
 *
 * 秘密情報（APIキー）を環境変数から安全に読み込み、
 * コード内に直書きしない設計パターンを学ぶ。
 *
 * 起動前に環境変数を設定してください：
 *   Windows (PowerShell): $env:NOTIFICATION_API_KEY="sk-test-abc123"
 *   Mac/Linux (bash):     export NOTIFICATION_API_KEY="sk-test-abc123"
 *
 * ※IntelliJの「実行構成」→「環境変数」でも設定可能
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("===== Day85 通知サービス（秘密情報管理） =====");
        System.out.println();

        // ① 設定を読み込む（環境変数から）
        AppConfig config = new AppConfig();

        // ② 起動時に設定状態を表示
        showStartupStatus(config);

        // ③ 依存オブジェクトを組み立てる（手動DI）
        NotificationClient client = new DummyNotificationClient();
        NotificationService service = new NotificationService(client, config);

        // ④ コマンドループ
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.print("操作を入力してください（helpで一覧、exitで終了）> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // ⑤ コマンドの解析（先頭の単語を取得）
            String command;
            String argument;
            int spaceIndex = line.indexOf(' ');
            if (spaceIndex > 0) {
                command = line.substring(0, spaceIndex).toLowerCase();
                argument = line.substring(spaceIndex + 1).trim();
            } else {
                command = line.toLowerCase();
                argument = "";
            }

            // ⑥ コマンドの振り分け
            switch (command) {
                case "send":
                    handleSend(service, argument);
                    break;
                case "config":
                    handleConfig(config);
                    break;
                case "help":
                    handleHelp(config);
                    break;
                case "exit":
                    System.out.println("アプリを終了します。");
                    scanner.close();
                    return;
                default:
                    System.out.println("[エラー] 不明なコマンドです: " + command);
                    System.out.println("  helpで操作一覧を確認できます。");
                    break;
            }
        }
    }

    /**
     * 起動時の設定状態を表示する。
     * APIキーが未設定の場合は警告メッセージを出す。
     */
    private static void showStartupStatus(AppConfig config) {
        System.out.println("[設定状態]");
        if (config.isApiKeyConfigured()) {
            System.out.println("  APIキー   : " + config.getMaskedApiKey() + "（設定済み）");
        } else {
            // ⑦ 警告：APIキーが未設定の場合、設定方法を案内する
            System.out.println("  [警告] APIキーが未設定です！");
            System.out.println("  送信機能を使うには、環境変数 " + config.getApiKeyEnvName() + " を設定してください。");
            System.out.println();
            System.out.println("  設定方法：");
            System.out.println("    Windows (PowerShell) : $env:" + config.getApiKeyEnvName() + "=\"your-api-key\"");
            System.out.println("    Mac/Linux (bash)     : export " + config.getApiKeyEnvName() + "=\"your-api-key\"");
            System.out.println("    IntelliJ             : 実行構成 → 環境変数 で追加");
        }
        System.out.println("  エンドポイント: " + config.getEndpoint());
    }

    /**
     * send コマンドの処理：メッセージを送信する。
     */
    private static void handleSend(NotificationService service, String message) {
        if (message.isEmpty()) {
            System.out.println("[エラー] メッセージが空です。使い方: send <メッセージ>");
            return;
        }
        try {
            service.send(message);
        } catch (SecretNotConfiguredException e) {
            // ⑧ 業務例外をキャッチして、ユーザー向けメッセージを表示
            System.out.println("[エラー] " + e.getMessage());
        }
    }

    /**
     * config コマンドの処理：現在の設定値を表示する。
     */
    private static void handleConfig(AppConfig config) {
        System.out.println("[現在の設定]");
        System.out.println("  APIキー       : " + config.getMaskedApiKey());
        System.out.println("  APIキー状態   : " + (config.isApiKeyConfigured() ? "設定済み" : "未設定"));
        System.out.println("  エンドポイント: " + config.getEndpoint());
        System.out.println("  環境変数名    : " + config.getApiKeyEnvName());
    }

    /**
     * help コマンドの処理：操作一覧を表示する。
     */
    private static void handleHelp(AppConfig config) {
        System.out.println("[操作一覧]");
        System.out.println("  send <メッセージ>  : 通知を送信する");
        System.out.println("  config             : 現在の設定を確認する");
        System.out.println("  help               : この一覧を表示する");
        System.out.println("  exit               : アプリを終了する");
        System.out.println();
        System.out.println("[環境変数の設定方法]");
        System.out.println("  APIキー : " + config.getApiKeyEnvName() + "=<値>");
        System.out.println("  エンドポイント : NOTIFICATION_ENDPOINT=<URL>（省略時はデフォルト値）");
    }
}
