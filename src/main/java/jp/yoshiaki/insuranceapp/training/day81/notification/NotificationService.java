package jp.yoshiaki.insuranceapp.training.day81.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 通知送信の業務ロジック。
 *
 * sendAsync()：@Async付き → 別スレッドで実行され、呼び出し元はすぐ戻る
 * sendSync() ：@Asyncなし → 同じスレッドで実行され、完了まで待つ（比較用）
 *
 * 【設計ポイント】
 * @Asyncメソッドの戻り値をCompletableFuture<T>にすることで、
 * 呼び出し元が「後から結果を取り出す」ことが可能になる。
 * void にすると結果も例外も取得できない（fire-and-forget方式）。
 */
@Service("day81NotificationService")
public class NotificationService {

    private final NotificationSender sender;
    private final InMemoryNotificationRepository repository;

    // ① コンストラクタインジェクション（Springが自動で依存を注入する）
    public NotificationService(NotificationSender sender,
                               InMemoryNotificationRepository repository) {
        this.sender = sender;
        this.repository = repository;
    }

    /**
     * 【非同期版】通知を送信する。
     *
     * @Async が付いているため、Springがこのメソッドを別スレッドで実行する。
     * 呼び出し元にはすぐ CompletableFuture が返り、結果は後から取得できる。
     *
     * 重要：@Async が効くのは「Springが管理するBeanのメソッド」かつ
     *       「外部から呼ばれた場合」のみ。同じクラス内からの呼び出し（this.sendAsync()）
     *       ではプロキシを経由しないため、非同期にならない。
     */
    @Async
    public CompletableFuture<Notification> sendAsync(String to, String message) {
        // ② 処理内容は同期版と同じ。@Asyncの仕組みで別スレッドになるだけ。
        Notification notification = doSend(to, message);
        // ③ CompletableFuture.completedFuture() で結果を包んで返す
        return CompletableFuture.completedFuture(notification);
    }

    /**
     * 【同期版】通知を送信する（比較用）。
     *
     * @Async が付いていないため、呼び出し元のスレッドでそのまま実行される。
     * 送信に2秒かかる間、呼び出し元は待たされる。
     */
    public Notification sendSync(String to, String message) {
        return doSend(to, message);
    }

    /**
     * 送信の共通処理。非同期版・同期版の両方から呼ばれる。
     */
    private Notification doSend(String to, String message) {
        Long id = repository.nextId();
        NotificationStatus status;
        LocalDateTime sentAt = LocalDateTime.now();

        try {
            // ④ 実際の送信処理（Fake実装では2秒待機）
            sender.send(to, message);
            status = NotificationStatus.SUCCESS;
        } catch (RuntimeException e) {
            // ⑤ 送信失敗時はFAILEDとして記録（例外をここで吸収する）
            System.out.println("  [エラー] 送信失敗: " + e.getMessage());
            status = NotificationStatus.FAILED;
        }

        Notification notification = new Notification(id, to, message, status, sentAt);
        repository.save(notification);
        return notification;
    }

    /**
     * 送信履歴を全件取得する。
     */
    public List<Notification> findAll() {
        return repository.findAll();
    }
}
