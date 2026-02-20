package jp.yoshiaki.insuranceapp.training.day81.notification;

import java.util.List;

/**
 * 通知の保存・取得を定義するinterface。
 * 今回はインメモリ実装だが、DB実装への差し替えも可能。
 */
public interface NotificationRepository {

    /**
     * 通知を保存する。
     *
     * @param notification 保存対象の通知
     */
    void save(Notification notification);

    /**
     * 全通知を取得する。
     *
     * @return 保存済みの全通知リスト
     */
    List<Notification> findAll();
}
