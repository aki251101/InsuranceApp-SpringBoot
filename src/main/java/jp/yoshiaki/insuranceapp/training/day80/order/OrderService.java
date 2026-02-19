package jp.yoshiaki.insuranceapp.training.day80.order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 注文に関する業務操作をまとめるサービスクラス。
 *
 * 2つの注文方法を提供する：
 * - placeOrder()      : 冪等キー付き（安全）
 * - placeOrderUnsafe(): 冪等キーなし（二重登録の危険あり）
 */
public class OrderService {

    private final List<Order> orders = new ArrayList<>();
    private final PaymentClient paymentClient;
    private final RetryExecutor retryExecutor;
    private int nextId = 1;

    // リトライ設定
    private static final int MAX_RETRIES = 3;         // 最大リトライ回数
    private static final long INITIAL_DELAY_MS = 500;  // 初回待機: 500ms

    public OrderService(PaymentClient paymentClient, RetryExecutor retryExecutor) {
        this.paymentClient = paymentClient;
        this.retryExecutor = retryExecutor;
    }

    /**
     * 冪等キー付きで注文を作成・確定する（安全なパターン）。
     *
     * 手順：
     * 1. 注文作成（PENDING）＋ 冪等キー（UUID）を生成
     * 2. リトライ付きで決済APIを呼び出す
     * 3. 成功 → 注文を確定（CONFIRMED）
     * 4. 全リトライ失敗 → 注文を失敗（FAILED）
     */
    public Order placeOrder(String productName) {
        // ① 冪等キーを生成（UUIDで世界中で重複しないIDを作る）
        String idempotencyKey = UUID.randomUUID().toString();
        Order order = new Order(nextId++, productName, idempotencyKey);

        System.out.println("注文を作成しました: " + order);
        System.out.println("冪等キー: " + idempotencyKey);
        System.out.println("決済APIを呼び出します（最大 " + (MAX_RETRIES + 1) + " 回試行）...");

        try {
            // ② リトライ付きで決済APIを呼ぶ（同じ冪等キーで再試行）
            String result = retryExecutor.executeWithRetry(
                () -> paymentClient.charge(productName, idempotencyKey),
                MAX_RETRIES,
                INITIAL_DELAY_MS
            );

            // ③ 決済成功 → 注文を確定
            order.confirm();
            orders.add(order);
            System.out.println("注文が確定しました: " + order);
            System.out.println("決済API処理件数: " + paymentClient.getProcessedCount() + " 件");
            return order;

        } catch (TransientException e) {
            // ④ 全リトライ失敗 → 注文を失敗にする
            order.fail();
            orders.add(order);
            System.out.println("注文が失敗しました（全リトライ失敗）: " + order);
            return order;
        }
    }

    /**
     * 冪等キーなしで注文を作成・確定する（危険なパターン：学習用）。
     *
     * リトライ時に冪等キーがないため、決済APIが毎回「新しいリクエスト」として処理する。
     * → 1回成功した後にリトライが走ると二重決済になる可能性がある。
     */
    public Order placeOrderUnsafe(String productName) {
        Order order = new Order(nextId++, productName, null); // 冪等キーなし

        System.out.println("【危険】冪等キーなしで注文を作成しました: " + order);
        System.out.println("決済APIを呼び出します（最大 " + (MAX_RETRIES + 1) + " 回試行）...");

        try {
            // 冪等キーなしで決済APIを呼ぶ
            String result = retryExecutor.executeWithRetry(
                () -> paymentClient.chargeUnsafe(productName),
                MAX_RETRIES,
                INITIAL_DELAY_MS
            );

            order.confirm();
            orders.add(order);
            System.out.println("注文が確定しました: " + order);
            System.out.println("決済API処理件数: " + paymentClient.getProcessedCount()
                + " 件 ← 注文数と一致していなければ二重決済の可能性あり！");
            return order;

        } catch (TransientException e) {
            order.fail();
            orders.add(order);
            System.out.println("注文が失敗しました（全リトライ失敗）: " + order);
            return order;
        }
    }

    /** 全注文の一覧を返す */
    public List<Order> listOrders() {
        return new ArrayList<>(orders);
    }
}
