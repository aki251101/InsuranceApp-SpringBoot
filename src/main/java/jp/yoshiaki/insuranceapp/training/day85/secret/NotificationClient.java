package jp.yoshiaki.insuranceapp.training.day85.secret;

/**
 * 外部通知APIへの送信を抽象化するインターフェース。
 *
 * interfaceにする理由：
 * - 本番用（HTTP通信）とテスト用（ダミー出力）を差し替え可能にする
 * - Serviceは「送信する」という契約だけ知ればよく、実装の詳細を知らなくてよい
 */
public interface NotificationClient {

    /**
     * 通知メッセージを外部APIに送信する。
     *
     * @param endpoint  送信先のURL（例："https://api.example.com/notify"）
     * @param apiKey    認証用のAPIキー
     * @param message   送信するメッセージ本文
     */
    void send(String endpoint, String apiKey, String message);
}
