package jp.yoshiaki.insuranceapp.training.day81.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知の送信結果を表すドメインクラス。
 * 一度作成したら変更しない不変オブジェクト。
 */
public class Notification {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Long id;
    private final String to;
    private final String message;
    private final NotificationStatus status;
    private final LocalDateTime sentAt;

    public Notification(Long id, String to, String message,
                        NotificationStatus status, LocalDateTime sentAt) {
        this.id = id;
        this.to = to;
        this.message = message;
        this.status = status;
        this.sentAt = sentAt;
    }

    // --- getter ---

    public Long getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    /**
     * 表示用の文字列。ステータスは日本語ラベルで出力する。
     * 例: [成功] admin: お知らせです (2026-02-20 10:00:05)
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s (%s)",
                status.getDisplayName(),
                to,
                message,
                sentAt.format(FORMATTER));
    }
}
