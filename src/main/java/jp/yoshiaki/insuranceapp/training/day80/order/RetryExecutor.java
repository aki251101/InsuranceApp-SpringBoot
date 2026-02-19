package jp.yoshiaki.insuranceapp.training.day80.order;

import java.util.function.Supplier;

/**
 * リトライ（再試行）を制御するユーティリティクラス。
 *
 * 指数バックオフ（exponential backoff）を実装している。
 * - 1回目の待機: initialDelayMs（例: 500ms）
 * - 2回目の待機: initialDelayMs × 2（例: 1000ms）
 * - 3回目の待機: initialDelayMs × 4（例: 2000ms）
 * → 相手サーバーへの負荷を徐々に減らし、回復時間を確保する
 *
 * 使い方：
 *   RetryExecutor executor = new RetryExecutor();
 *   String result = executor.executeWithRetry(
 *       () -> paymentClient.charge("商品名", key),  // 実行したい処理
 *       3,    // 最大リトライ回数
 *       500   // 初回待機ミリ秒
 *   );
 */
public class RetryExecutor {

    /**
     * 渡された処理を最大 maxRetries 回リトライする。
     *
     * @param action        実行する処理（Supplier: 引数なし、戻り値あり）
     * @param maxRetries    最大リトライ回数（例: 3なら初回＋リトライ3回＝最大4回実行）
     * @param initialDelayMs 初回の待機時間（ミリ秒）。以降は倍々に増える
     * @return 処理の成功結果
     * @throws TransientException 全リトライ失敗時は最後の例外をそのまま投げる
     */
    public String executeWithRetry(Supplier<String> action, int maxRetries, long initialDelayMs) {

        // ① 最後に発生した例外を保持する変数
        TransientException lastException = null;

        // ② 現在の待機時間（リトライごとに倍にしていく）
        long currentDelay = initialDelayMs;

        // ③ 初回実行 + maxRetries回のリトライ = 合計 (maxRetries + 1) 回の試行
        for (int attempt = 0; attempt <= maxRetries; attempt++) {

            try {
                // 処理を実行する
                String result = action.get();

                // 成功した場合：リトライ回数がわかるようログを出す
                if (attempt > 0) {
                    System.out.println("  [リトライ] " + attempt + "回目のリトライで成功しました");
                }
                return result;

            } catch (TransientException e) {
                // 一時的障害 → リトライ対象
                lastException = e;

                if (attempt < maxRetries) {
                    // まだリトライ回数が残っている → 待機してリトライ
                    System.out.println("  [リトライ] 試行 " + (attempt + 1) + "/"
                        + (maxRetries + 1) + " 失敗: " + e.getMessage());
                    System.out.println("  [リトライ] " + currentDelay + "ms 後にリトライします...");

                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        // スレッド割り込み → 中断フラグを復元して終了
                        Thread.currentThread().interrupt();
                        throw new TransientException("リトライ中に割り込みが発生しました", ie);
                    }

                    // ④ 指数バックオフ：待機時間を2倍にする
                    currentDelay *= 2;

                } else {
                    // リトライ回数を使い切った → 最終失敗
                    System.out.println("  [リトライ] 試行 " + (attempt + 1) + "/"
                        + (maxRetries + 1) + " 失敗: " + e.getMessage());
                    System.out.println("  [リトライ] 全 " + (maxRetries + 1)
                        + " 回の試行が失敗しました");
                }
            }
        }

        // ⑤ 全リトライ失敗 → 最後の例外をそのまま投げる
        throw lastException;
    }
}
