package jp.yoshiaki.insuranceapp.training.day81.notification;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * コンソールアプリのメインクラス。
 * CommandLineRunnerを実装し、Spring Boot起動後に自動的にrun()が呼ばれる。
 *
 * 【コマンド一覧】
 *   send <宛先> <メッセージ>       : 非同期で通知を送信（すぐ戻る）
 *   send-sync <宛先> <メッセージ>  : 同期で通知を送信（完了まで待つ）
 *   result                          : 直近の非同期送信の結果を確認
 *   list                            : 送信履歴を一覧表示
 *   help                            : コマンド一覧を表示
 *   exit                            : 終了
 */
@Component("day81AppRunner")
public class AppRunner implements CommandLineRunner {

    private final NotificationService service;

    // ① 直近の非同期送信結果を保持するフィールド
    //    CompletableFuture は「将来届く荷物の追跡番号」のようなもの
    private CompletableFuture<Notification> lastFuture = null;

    public AppRunner(NotificationService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===================================");
        System.out.println("  通知管理ミニアプリ（Day81）");
        System.out.println("  非同期処理の体験版");
        System.out.println("===================================");
        System.out.println();
        printHelp();

        while (true) {
            System.out.println();
            System.out.print("操作を入力してください > ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // ② コマンドとパラメータを分割
            String[] parts = line.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "send":
                    handleSendAsync(parts);
                    break;
                case "send-sync":
                    handleSendSync(parts);
                    break;
                case "result":
                    handleResult();
                    break;
                case "list":
                    handleList();
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("アプリを終了します。");
                    return;
                default:
                    System.out.println("不明なコマンドです。help でコマンド一覧を確認できます。");
                    break;
            }
        }
    }

    /**
     * 非同期送信を実行する。
     * @Async メソッドを呼ぶので、すぐに制御が戻る。
     * 結果は lastFuture に保持し、result コマンドで確認する。
     */
    private void handleSendAsync(String[] parts) {
        if (parts.length < 3) {
            System.out.println("使い方: send <宛先> <メッセージ>");
            System.out.println("  例: send admin お知らせです");
            return;
        }

        String to = parts[1];
        String message = parts[2];

        System.out.println("--- 非同期送信を開始します ---");
        long startTime = System.currentTimeMillis();

        // ③ @Async メソッドを呼ぶ → Springが別スレッドで実行
        //    戻り値のCompletableFutureは「まだ結果が入っていない箱」
        lastFuture = service.sendAsync(to, message);

        // ④ ここに到達するのは「すぐ」（2秒待たない）
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("送信を受け付けました（受付所要時間: " + elapsed + "ms）");
        System.out.println("※ 裏で送信処理が進行中です。result コマンドで結果を確認できます。");

        // ⑤ 非同期なので、例外が起きても「ここ」には飛んでこない
        //    例外は CompletableFuture の中に格納される
        lastFuture.exceptionally(ex -> {
            System.out.println("  [非同期エラー通知] " + ex.getMessage());
            return null;
        });
    }

    /**
     * 同期送信を実行する（比較用）。
     * 送信が完了するまでメインスレッドが止まる（2秒待つ）。
     */
    private void handleSendSync(String[] parts) {
        if (parts.length < 3) {
            System.out.println("使い方: send-sync <宛先> <メッセージ>");
            System.out.println("  例: send-sync admin お知らせです");
            return;
        }

        String to = parts[1];
        String message = parts[2];

        System.out.println("--- 同期送信を開始します（完了まで待ちます）---");
        long startTime = System.currentTimeMillis();

        // ⑥ @Async なしのメソッド → 同じスレッドで実行、完了まで待つ
        Notification result = service.sendSync(to, message);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("送信が完了しました（所要時間: " + elapsed + "ms）");
        System.out.println("  結果: " + result);
    }

    /**
     * 直近の非同期送信の結果を確認する。
     * CompletableFuture.getNow() で「今の時点で結果が入っているか」を確認。
     */
    private void handleResult() {
        if (lastFuture == null) {
            System.out.println("まだ非同期送信を実行していません。send コマンドで送信してください。");
            return;
        }

        // ⑦ isDone() で完了済みかどうかを確認
        if (lastFuture.isDone()) {
            try {
                // ⑧ get() で結果を取り出す。isDone()==true なのでブロックしない
                Notification result = lastFuture.get();
                if (result != null) {
                    System.out.println("非同期送信の結果（完了済み）:");
                    System.out.println("  " + result);
                } else {
                    System.out.println("非同期送信は完了しましたが、結果がnullです（例外が発生した可能性）");
                }
            } catch (Exception e) {
                System.out.println("非同期送信で例外が発生しました: " + e.getMessage());
            }
        } else {
            // ⑨ まだ処理中の場合
            System.out.println("非同期送信はまだ処理中です... もう少し待ってから再度 result を実行してください。");
        }
    }

    /**
     * 送信履歴を一覧表示する。
     */
    private void handleList() {
        List<Notification> all = service.findAll();
        if (all.isEmpty()) {
            System.out.println("送信履歴はまだありません。");
            return;
        }

        System.out.println("--- 送信履歴（全" + all.size() + "件）---");
        for (Notification n : all) {
            System.out.println("  " + n);
        }
    }

    /**
     * コマンド一覧を表示する。
     */
    private void printHelp() {
        System.out.println("【コマンド一覧】");
        System.out.println("  send <宛先> <メッセージ>       … 非同期で通知を送信（すぐ戻る）");
        System.out.println("  send-sync <宛先> <メッセージ>  … 同期で通知を送信（完了まで待つ）");
        System.out.println("  result                          … 直近の非同期送信の結果を確認");
        System.out.println("  list                            … 送信履歴を一覧表示");
        System.out.println("  help                            … このヘルプを表示");
        System.out.println("  exit                            … 終了");
    }
}
