package jp.yoshiaki.insuranceapp.training.day72.book;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * メモリ上で書籍を保管する Repository 実装。
 * DB を使わずに REST 設計の学習に集中するための簡易実装。
 */
@Repository("day72InMemoryBookRepository")
public class InMemoryBookRepository implements BookRepository {

    // ① ConcurrentHashMap：スレッドセーフな Map（Web アプリは複数リクエストが同時に来る）
    private final Map<Long, Book> store = new ConcurrentHashMap<>();

    // ② AtomicLong：スレッドセーフな ID 採番カウンター
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Book save(Book book) {
        // ID が null なら新規登録、あれば更新
        if (book.getId() == null) {
            // リフレクションを避け、新しい Book を作り直して保存
            Book newBook = new Book(sequence.getAndIncrement(), book.getTitle(), book.getIsbn());
            store.put(newBook.getId(), newBook);
            return newBook;
        } else {
            store.put(book.getId(), book);
            return book;
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return store.values().stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
