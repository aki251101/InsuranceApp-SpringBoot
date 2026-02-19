package jp.yoshiaki.insuranceapp.training.day80.order;

import java.util.List;
import java.util.Scanner;

/**
 * Day80: 再試行/冪等性入門 ─ 注文管理コンソールアプリ
 *
 * 外部決済APIの呼び出しを模擬し、リトライと冪等性の効果を体験する。
 * - order <商品名>       : 冪等キー付き注文（安全）
 * - order-unsafe <商品名>: 冪等キーなし注文（二重登録の危険あり）
 * - list                 : 注文一覧を表示
 * - help                 : 操作一覧を表示
 * - exit                 : 終了
 */
public class Main {

    public static void main(String[] args) {
        // ① 決済APIクライアント（失敗率50%で一時障害をシミュレート）
        PaymentClient paymentClient = new PaymentClient(0.5);

        // ② リトライ実行器
        RetryExecutor retryExecutor = new RetryExecutor();

        // ③ 注文サービス（決済クライアントとリトライ実行器を注入）
        OrderService orderService = new OrderService(paymentClient, retryExecutor);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 注文管理システム（Day80: リトライ＋冪等性の体験） ===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // コマンドと引数を分離
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = (parts.length > 1) ? parts[1] : null;

            switch (command) {
                case "order":
                    // 冪等キー付き注文（安全）
                    if (argument == null || argument.isEmpty()) {
                        System.out.println("エラー: 商品名を指定してください（例: order カレー）");
                    } else {
                        System.out.println();
                        orderService.placeOrder(argument);
                        System.out.println();
                    }
                    break;

                case "order-unsafe":
                    // 冪等キーなし注文（危険：学習用）
                    if (argument == null || argument.isEmpty()) {
                        System.out.println("エラー: 商品名を指定してください（例: order-unsafe ラーメン）");
                    } else {
                        System.out.println();
                        orderService.placeOrderUnsafe(argument);
                        System.out.println();
                    }
                    break;

                case "list":
                    // 注文一覧を表示
                    List<Order> orders = orderService.listOrders();
                    if (orders.isEmpty()) {
                        System.out.println("注文はまだありません。");
                    } else {
                        System.out.println("--- 注文一覧 ---");
                        for (Order order : orders) {
                            System.out.println("  " + order);
                        }
                        System.out.println("合計: " + orders.size() + " 件");
                    }
                    break;

                case "help":
                    System.out.println("--- 操作一覧 ---");
                    System.out.println("  order <商品名>        : 冪等キー付きで注文する（安全）");
                    System.out.println("  order-unsafe <商品名> : 冪等キーなしで注文する（二重登録の危険あり）");
                    System.out.println("  list                  : 注文一覧を表示する");
                    System.out.println("  help                  : この一覧を表示する");
                    System.out.println("  exit                  : 終了する");
                    break;

                case "exit":
                    System.out.println("注文管理システムを終了します。");
                    scanner.close();
                    return;

                default:
                    System.out.println("不明なコマンドです: " + command + "（helpで一覧を表示）");
                    break;
            }
        }
    }
}
