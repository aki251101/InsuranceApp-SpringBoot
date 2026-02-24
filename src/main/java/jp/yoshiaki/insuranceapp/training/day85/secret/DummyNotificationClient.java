package jp.yoshiaki.insuranceapp.training.day85.secret;

/**
 * NotificationClientのダミー実装。
 *
 * 実際のHTTP通信は行わず、コンソール出力で送信をシミュレーションする。
 * 学習や開発初期の段階で「外部APIがなくても動作確認できる」ために使う。
 *
 * 実務での使いどころ：
 * - 外部APIがまだ用意されていない開発初期
 * - 単体テスト（実際にメールやSlack通知を飛ばしたくない）
 * - ローカル開発環境での動作確認
 */
public class DummyNotificationClient implements NotificationClient {

    /**
     * 通知送信のシミュレーション。
     * 実際のHTTP通信の代わりに、送信内容をコンソールに出力する。
     *
     * @param endpoint  送信先URL
     * @param apiKey    APIキー（マスクして表示）
     * @param message   送信メッセージ
     */
    @Override
    public void send(String endpoint, String apiKey, String message) {
        // ① 送信シミュレーション：実務ではここがHttpClient等のHTTP通信になる
        System.out.println("[送信シミュレーション]");
        System.out.println("  送信先   : " + endpoint);
        // ② APIキーは全文表示しない（先頭4文字だけ見せる安全策）
        System.out.println("  認証キー : " + maskKey(apiKey));
        System.out.println("  メッセージ: " + message);
        System.out.println("  結果     : 送信成功（ダミー）");
    }

    /**
     * APIキーをマスクする内部メソッド。
     * ログ出力時に全文を露出させない安全策。
     */
    private String maskKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }
        return key.substring(0, 4) + "****";
    }
}
