package jp.yoshiaki.insuranceapp.training.day76.oauth;

import java.util.List;
import java.util.Scanner;

/**
 * Day76：OAuth 2.0 認可コードフロー シミュレーション
 *
 * Googleカレンダー連携で使われるOAuth 2.0の流れを、
 * 外部APIなしでCLI上で体験するミニアプリ。
 *
 * コマンド一覧：
 *   auth     - 認可フローを実行（認可コード取得 → トークン交換）
 *   token    - 現在のトークン状態を確認
 *   register - イベントを登録（認証済みの場合のみ）
 *   list     - 登録済みイベント一覧
 *   revoke   - トークンを無効化（連携解除）
 *   help     - コマンド一覧を表示
 *   exit     - 終了
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 依存オブジェクトの組み立て（実務ではSpringのDIが自動で行う）
        TokenStore tokenStore = new TokenStore();
        FakeAuthServer authServer = new FakeAuthServer();
        OAuthService oauthService = new OAuthService(authServer, tokenStore);
        CalendarClient calendarClient = new CalendarClient(tokenStore);

        System.out.println("=== Day76: OAuth 2.0 カレンダー連携シミュレーション ===");
        System.out.println();
        System.out.println("Googleカレンダー連携で使われるOAuth 2.0の認可フローを体験します。");
        System.out.println("まず auth コマンドで認証を行い、その後 register でイベントを登録してみましょう。");
        System.out.println("（アクセストークンは60秒で期限切れになります。期限切れも体験してみてください）");
        System.out.println();
        printHelp();

        while (true) {
            System.out.println();
            System.out.print("操作を入力してください > ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // コマンドと引数を分割
            String[] parts = line.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "auth":
                        oauthService.authorize();
                        break;

                    case "token":
                        System.out.println(oauthService.showTokenStatus());
                        break;

                    case "register":
                        handleRegister(parts, calendarClient);
                        break;

                    case "list":
                        handleList(calendarClient);
                        break;

                    case "revoke":
                        oauthService.revoke();
                        break;

                    case "help":
                        printHelp();
                        break;

                    case "exit":
                        System.out.println("終了します。お疲れさまでした！");
                        scanner.close();
                        return;

                    default:
                        System.out.println("  不明なコマンドです: " + command);
                        System.out.println("  help で使えるコマンドを確認してください。");
                        break;
                }
            } catch (UnauthorizedException e) {
                // OAuth未認証・期限切れのエラーをキャッチして案内
                System.out.println("  【認証エラー】" + e.getMessage());
            } catch (IllegalArgumentException e) {
                // 入力値の不正をキャッチ
                System.out.println("  【入力エラー】" + e.getMessage());
            }
        }
    }

    /**
     * register コマンドの処理。
     * 書式: register <タイトル> <日付(YYYY-MM-DD)>
     */
    private static void handleRegister(String[] parts, CalendarClient calendarClient) {
        if (parts.length < 3) {
            System.out.println("  使い方: register <タイトル> <日付(YYYY-MM-DD)>");
            System.out.println("  例: register 満期確認 2026-03-15");
            return;
        }

        String title = parts[1];
        String date = parts[2];

        // 日付の簡易バリデーション
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("  日付はYYYY-MM-DD形式で入力してください。例: 2026-03-15");
            return;
        }

        calendarClient.registerEvent(title, date);
    }

    /**
     * list コマンドの処理。
     */
    private static void handleList(CalendarClient calendarClient) {
        List<CalendarEvent> events = calendarClient.listEvents();

        if (events.isEmpty()) {
            System.out.println("  登録済みイベントはありません。");
            return;
        }

        System.out.println("  --- 登録済みイベント一覧 ---");
        for (CalendarEvent event : events) {
            System.out.println(event);
        }
        System.out.println("  合計: " + events.size() + "件");
    }

    /**
     * コマンド一覧を表示する。
     */
    private static void printHelp() {
        System.out.println("--- コマンド一覧 ---");
        System.out.println("  auth                         : 認可フローを実行（Google認証のシミュレーション）");
        System.out.println("  token                        : 現在のトークン状態を確認");
        System.out.println("  register <タイトル> <日付>    : カレンダーにイベントを登録（例: register 満期確認 2026-03-15）");
        System.out.println("  list                         : 登録済みイベント一覧を表示");
        System.out.println("  revoke                       : トークンを無効化（連携解除）");
        System.out.println("  help                         : このヘルプを表示");
        System.out.println("  exit                         : 終了");
    }
}
