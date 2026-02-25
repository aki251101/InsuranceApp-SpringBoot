package jp.yoshiaki.insuranceapp.training.day86.order;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OrderRepositoryのインメモリ実装。
 * ConcurrentHashMapでデータを保持する（DBの代わり）。
 * AtomicLongでIDを自動採番する。
 */
@Repository("day86InMemoryOrderRepository") // ① Bean名を明示（他Dayとの衝突防止）
public class InMemoryOrderRepository implements OrderRepository {

    // ② ConcurrentHashMap：スレッドセーフなMap（複数リクエストが同時に来ても安全）
    private final Map<Long, Order> store = new ConcurrentHashMap<>();

    // ③ AtomicLong：スレッドセーフな連番生成器（1, 2, 3... と自動で増える）
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Order save(Order order) {
        // IDが未設定の場合（新規作成）は自動採番
        if (order.getId() == null) {
            // Orderのidフィールドに採番した値を設定するため、新しいOrderを作成
            Order newOrder = new Order(sequence.getAndIncrement(), order.getItemName(), order.getQuantity());
            store.put(newOrder.getId(), newOrder);
            return newOrder;
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        // Map.get()はキーがなければnullを返す → Optional.ofNullable()でラップ
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        // Map.values()で全件取得し、新しいListにコピーして返す
        return new ArrayList<>(store.values());
    }
}
