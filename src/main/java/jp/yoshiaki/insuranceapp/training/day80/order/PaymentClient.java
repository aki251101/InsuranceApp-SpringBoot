package jp.yoshiaki.insuranceapp.training.day80.order;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 外部の決済APIを模擬するクラス。
 *
 * 実務では HttpClient や RestTemplate で外部サーバーを呼ぶが、
 * 今回は学習用に「ランダムで失敗する」動きをシミュレートする。
 *
 * 冪等キー（idempotency key）を使うことで、
 * 同じリクエストを複数回送っても二重処理を防げる仕組みを持つ。
 */
public class PaymentClient {

    private final Random random = new Random();

    // ① 処理済みの冪等キーを記録するセット（重複チェック用）
    private final Set<String> processedKeys = new HashSet<>();

    // ② 処理された決済件数（二重登録の可視化用）
    private int processedCount = 0;

    // ③ 失敗確率（0.0〜1.0）。0.5なら約50%で失敗する
    private final double failureRate;

    public PaymentClient(double failureRate) {
        this.failureRate = failureRate;
    }

    /**
     * 冪等キー付きの決済処理（安全）。
     *
     * - 冪等キーが処理済みなら「既に処理済み」として成功を返す（二重処理しない）
     * - 未処理ならランダムで成功/失敗を判定する
     * - 成功時は冪等キーを記録する
     */
    public String charge(String productName, String idempotencyKey) {
        // 冪等キーが既に処理済みかチェック
        if (processedKeys.contains(idempotencyKey)) {
            System.out.println("  [決済API] 冪等キー " + idempotencyKey.substring(0, 8)
                + "... は処理済み → 二重処理をスキップ（安全）");
            return "決済完了（処理済みキーのため二重処理なし）";
        }

        // ランダムで一時的障害を発生させる
        if (random.nextDouble() < failureRate) {
            throw new TransientException(
                "決済API一時障害: " + productName + " の決済処理がタイムアウトしました"
            );
        }

        // 成功 → 冪等キーを記録して二重処理を防止
        processedKeys.add(idempotencyKey);
        processedCount++;
        System.out.println("  [決済API] " + productName + " の決済が成功しました"
            + "（冪等キー: " + idempotencyKey.substring(0, 8) + "...）");
        return "決済完了";
    }

    /**
     * 冪等キーなしの決済処理（危険：二重登録の可能性あり）。
     *
     * 呼ばれるたびに無条件で処理件数をカウントする。
     * リトライで複数回成功すると、同じ注文が何度も決済される。
     */
    public String chargeUnsafe(String productName) {
        // ランダムで一時的障害を発生させる
        if (random.nextDouble() < failureRate) {
            throw new TransientException(
                "決済API一時障害: " + productName + " の決済処理がタイムアウトしました"
            );
        }

        // 成功 → 毎回カウントされる（冪等キーがないため重複チェックなし）
        processedCount++;
        System.out.println("  [決済API] " + productName
            + " の決済が成功しました（冪等キーなし → 重複チェックなし）");
        return "決済完了";
    }

    /** 処理された決済件数を返す（二重登録の確認用） */
    public int getProcessedCount() {
        return processedCount;
    }
}
