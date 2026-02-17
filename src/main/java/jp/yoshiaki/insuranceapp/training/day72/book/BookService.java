package jp.yoshiaki.insuranceapp.training.day72.book;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 書籍の業務ロジック。
 * ISBN 重複チェック・存在確認を行い、ルール違反時は業務例外を throw する。
 */
@Profile("training")
@Service("day72BookService")
public class BookService {

    private final BookRepository bookRepository;

    // ① コンストラクタ注入：Spring が InMemoryBookRepository を自動で渡してくれる
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 書籍を新規登録する。
     * ISBN が既に存在する場合は DuplicateIsbnException を throw。
     */
    public Book create(String title, String isbn) {
        // ② ISBN 重複チェック
        bookRepository.findByIsbn(isbn).ifPresent(existing -> {
            throw new DuplicateIsbnException(
                    "ISBN=" + isbn + " は既に登録されています（書籍名: " + existing.getTitle() + "）");
        });

        Book book = new Book(null, title, isbn);
        return bookRepository.save(book);
    }

    /** 全件取得 */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * ID で 1件取得。
     * 見つからなければ BookNotFoundException を throw。
     */
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("ID=" + id + " の書籍が見つかりません"));
    }

    /**
     * 書籍情報を更新する。
     * 存在しなければ 404、ISBN が他の書籍と重複すれば 409。
     */
    public Book update(Long id, String title, String isbn) {
        // ③ まず存在確認
        Book existing = findById(id);

        // ④ ISBN 重複チェック（自分自身は除外する）
        bookRepository.findByIsbn(isbn).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateIsbnException(
                        "ISBN=" + isbn + " は既に別の書籍（ID=" + other.getId() + "）で登録されています");
            }
        });

        existing.update(title, isbn);
        return bookRepository.save(existing);
    }

    /**
     * 書籍を削除する。
     * 存在しなければ BookNotFoundException を throw。
     */
    public void delete(Long id) {
        // ⑤ 存在確認してから削除（存在しないIDの削除は 404）
        findById(id);
        bookRepository.deleteById(id);
    }
}
