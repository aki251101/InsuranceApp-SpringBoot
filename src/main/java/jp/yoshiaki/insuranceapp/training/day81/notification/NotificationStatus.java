package jp.yoshiaki.insuranceapp.training.day81.notification;

/**
 * 通知の送信状態を表すenum。
 * 表示時は日本語ラベルに変換する。
 */
public enum NotificationStatus {

    PENDING("送信中"),
    SUCCESS("成功"),
    FAILED("失敗");

    private final String displayName;

    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }

    /** 日本語の表示ラベルを返す */
    public String getDisplayName() {
        return displayName;
    }
}
