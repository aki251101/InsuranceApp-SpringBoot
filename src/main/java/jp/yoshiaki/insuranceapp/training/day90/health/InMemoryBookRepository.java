package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BookRepository のインメモリ実装。
 * ConcurrentHashMap で書籍を保持する（学習用：DB不要）。
 *
 * isAvailable() は通常 true を返すが、
 * テスト時に false に切り替えて「ダウン状態」を再現できる。
 */
@Repository("day90InMemoryBookRepository")  // ① Bean名を明示（他Dayとの衝突防止）
public class InMemoryBookRepository implements BookRepository {

    private final Map<Long, Book> store = new ConcurrentHashMap<>();  // ② スレッドセーフなMap
    private final AtomicLong idCounter = new AtomicLong(1L);          // ③ ID自動採番
    private boolean available = true;  // ④ ヘルスチェック用フラグ（デフォルト：正常）

    @Override
    public Book save(Book book) {
        // IDを自動採番して新しいBookを作成
        Long newId = idCounter.getAndIncrement();
        Book saved = new Book(newId, book.getTitle(), book.getAuthor(), book.getStock());
        store.put(newId, saved);
        return saved;
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * データストアの利用可否を切り替える（テスト・デバッグ用）。
     * false にすると、ヘルスチェックが DOWN を返すようになる。
     *
     * @param available true=正常, false=異常
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
