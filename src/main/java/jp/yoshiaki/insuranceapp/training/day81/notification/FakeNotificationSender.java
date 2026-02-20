package jp.yoshiaki.insuranceapp.training.day81.notification;

import org.springframework.stereotype.Component;

/**
 * NotificationSenderのFake実装。
 * 実際の外部APIの代わりに、Thread.sleep で「2秒かかる送信処理」を模擬する。
 *
 * 宛先が "error" の場合は意図的に例外を投げる（例外テスト用）。
 */
@Component("day81FakeNotificationSender")
public class FakeNotificationSender implements NotificationSender {

    // ① 送信にかかる時間（ミリ秒）。外部APIの応答待ちを模擬する。
    private static final long SIMULATE_DELAY_MS = 2000;

    @Override
    public String send(String to, String message) {
        // ② 宛先が "error" なら意図的に例外を投げる（非同期の例外伝播を体験するため）
        if ("error".equalsIgnoreCase(to)) {
            throw new RuntimeException("送信先 '" + to + "' への送信に失敗しました（疑似エラー）");
        }

        try {
            // ③ 外部APIの応答待ちを模擬（2秒停止）
            System.out.println("  [送信処理] " + Thread.currentThread().getName()
                    + " で送信開始... (2秒かかります)");
            Thread.sleep(SIMULATE_DELAY_MS);
        } catch (InterruptedException e) {
            // ④ スレッドの割り込みが来た場合、割り込みフラグを復元して例外を投げる
            Thread.currentThread().interrupt();
            throw new RuntimeException("送信処理が中断されました", e);
        }

        System.out.println("  [送信処理] " + Thread.currentThread().getName() + " で送信完了!");
        return "送信完了";
    }
}
