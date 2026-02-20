package jp.yoshiaki.insuranceapp.training.day81.notification;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * NotificationRepositoryのインメモリ実装。
 *
 * 非同期処理では複数スレッドから同時にアクセスされる可能性があるため、
 * スレッドセーフなConcurrentHashMapとAtomicLongを使用する。
 * （通常のHashMapだと、同時書き込みでデータが壊れるリスクがある）
 */
@Repository("day81InMemoryNotificationRepository")
public class InMemoryNotificationRepository implements NotificationRepository {

    // ① ConcurrentHashMap：複数スレッドから同時にアクセスしても安全なMap
    private final Map<Long, Notification> store = new ConcurrentHashMap<>();

    // ② AtomicLong：複数スレッドからインクリメントしても値が飛ばないカウンター
    private final AtomicLong sequence = new AtomicLong(1L);

    /**
     * 新しいIDを自動採番して返す。
     * AtomicLongなので、2つのスレッドが同時に呼んでも同じ番号にはならない。
     */
    public Long nextId() {
        return sequence.getAndIncrement();
    }

    @Override
    public void save(Notification notification) {
        store.put(notification.getId(), notification);
    }

    @Override
    public List<Notification> findAll() {
        return new ArrayList<>(store.values());
    }
}
