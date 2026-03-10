package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 商品Repository実装（InMemory：メモリ上で保持）
 *
 * FaultSimulatorのDB障害フラグがONの場合、各操作でRuntimeExceptionを投げる。
 * これにより「DB接続が切れたときにアプリがどう振る舞うか」をスモークテストで体験できる。
 */
@Repository("day99InMemoryProductRepository")
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new LinkedHashMap<>(); // ① 登録順を保持
    private final AtomicLong idGenerator = new AtomicLong(1L);      // ② スレッドセーフなID採番
    private final FaultSimulator faultSimulator;

    // コンストラクタインジェクション
    public InMemoryProductRepository(FaultSimulator faultSimulator) {
        this.faultSimulator = faultSimulator;
    }

    @Override
    public List<Product> findAll() {
        checkDbFault(); // 障害チェック
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findById(Long id) {
        checkDbFault();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Product save(Product product) {
        checkDbFault();
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement()); // ③ 新規ならIDを自動採番
        }
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public long count() {
        checkDbFault();
        return store.size();
    }

    /**
     * DB障害フラグがONなら例外を投げる
     * 実際のDB障害では java.sql.SQLException や
     * org.springframework.dao.DataAccessException が発生するが、
     * 学習用として RuntimeException で疑似する。
     */
    private void checkDbFault() {
        if (faultSimulator.isDbFault()) {
            throw new RuntimeException("DB接続失敗（シミュレーション）: データベースに接続できません");
        }
    }
}
