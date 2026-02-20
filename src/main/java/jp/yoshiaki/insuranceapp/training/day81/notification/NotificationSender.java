package jp.yoshiaki.insuranceapp.training.day81.notification;

/**
 * 通知の送信処理を定義するinterface。
 * 実装を差し替えることで、本物のメール送信やSMS送信にも対応できる。
 * 今回はFake実装（疑似送信）で「時間がかかる外部連携」を模擬する。
 */
public interface NotificationSender {

    /**
     * 通知を送信する。
     *
     * @param to      宛先（例："admin"）
     * @param message 本文（例："お知らせです"）
     * @return 送信結果の文字列（例："送信完了"）
     * @throws RuntimeException 送信に失敗した場合
     */
    String send(String to, String message);
}
