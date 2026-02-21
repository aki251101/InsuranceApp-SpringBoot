package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HashMapを使ったインメモリのRepository実装。
 *
 * ID採番はAtomicLongで自動連番。
 * 開発・学習用のため、アプリ終了時にデータは消える。
 */
// @Repository("day82InMemoryProductRepository")  ← Spring Boot利用時はこのBean名で登録
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // ① 商品を保存する（新規登録のみ。IDは自動採番）
    @Override
    public Product save(Product product) {
        long newId = idGenerator.getAndIncrement();
        Product saved = new Product(newId, product.getName(),
                product.getCategory(), product.getDescription());
        store.put(newId, saved);
        return saved;
    }

    // ② IDで検索する
    @Override
    public Optional<Product> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    // ③ 全件取得する
    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }
}
