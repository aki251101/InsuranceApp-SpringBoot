package jp.yoshiaki.insuranceapp.training.day73.deadline;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * メモリ上にタスクを保存するRepository実装
 *
 * ConcurrentHashMap を使うことで、
 * 定期実行（Scheduler）と REST API が同時にアクセスしても安全。
 * （スレッドセーフ = 複数の処理が同時に動いてもデータが壊れない）
 */
@Repository("day73InMemoryDeadlineRepository")
public class InMemoryDeadlineRepository implements DeadlineRepository {

    // ① データ保存用のMap（キー=ID、値=Deadline）
    //    ConcurrentHashMap = 複数スレッドから同時アクセスしても安全なMap
    private final Map<Long, Deadline> store = new ConcurrentHashMap<>();

    // ② ID採番用のカウンター
    //    AtomicLong = 複数スレッドから同時にインクリメントしても安全
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Deadline save(Deadline deadline) {
        // IDが未設定なら新規採番
        if (deadline.getId() == null) {
            deadline.setId(sequence.getAndIncrement());
        }
        store.put(deadline.getId(), deadline);
        return deadline;
    }

    @Override
    public Optional<Deadline> findById(Long id) {
        // Mapから検索。無ければ空のOptionalを返す
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Deadline> findAll() {
        // Mapの全値をリストにして返す
        return new ArrayList<>(store.values());
    }
}
