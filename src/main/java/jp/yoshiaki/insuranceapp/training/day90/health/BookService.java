package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 書籍の業務ロジック。
 * 書籍の作成と一覧取得を提供する。
 */
@Service("day90BookService")  // Bean名を明示（他Dayとの衝突防止）
public class BookService {

    private final BookRepository repository;

    // ── コンストラクタインジェクション ──
    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    /**
     * 書籍を新規作成する。
     *
     * @param title  タイトル
     * @param author 著者
     * @param stock  在庫数
     * @return 保存された書籍（ID採番済み）
     */
    public Book create(String title, String author, int stock) {
        Book book = new Book(null, title, author, stock);
        return repository.save(book);
    }

    /**
     * 全書籍を取得する。
     *
     * @return 書籍のリスト
     */
    public List<Book> findAll() {
        return repository.findAll();
    }
}
