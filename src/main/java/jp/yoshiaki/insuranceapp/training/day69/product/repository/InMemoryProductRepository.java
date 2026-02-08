package jp.yoshiaki.insuranceapp.training.day69.product.repository;

import jp.yoshiaki.insuranceapp.training.day69.product.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * メモリ上のMapで商品を保管するRepository実装。
 * テストではこのクラスは使わない（ServiceをMockBeanで差し替えるため）。
 * 手動でPostman確認したい場合のために用意している。
 */
@Repository("day69InMemoryProductRepository")
public class InMemoryProductRepository implements ProductRepository {

    // スレッドセーフなMapでデータを保持
    private final Map<Long, Product> store = new ConcurrentHashMap<>();

    // ID自動採番用のカウンター
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Product save(Product product) {
        // IDが未採番なら新規採番する
        if (product.getId() == null) {
            product.setId(sequence.getAndIncrement());
        }
        store.put(product.getId(), product);
        return product;
    }
}
