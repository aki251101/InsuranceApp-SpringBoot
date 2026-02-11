package jp.yoshiaki.insuranceapp.training.day72.book;

import java.util.List;
import java.util.Optional;

/**
 * 書籍の永続化窓口（interface）。
 * 実装を差し替え可能にするため、interface で定義する。
 */
public interface BookRepository {

    /** 書籍を保存（新規登録・更新兼用） */
    Book save(Book book);

    /** IDで1件取得。見つからなければ Optional.empty() */
    Optional<Book> findById(Long id);

    /** 全件取得 */
    List<Book> findAll();

    /** ISBNで検索。重複チェックに使う */
    Optional<Book> findByIsbn(String isbn);

    /** IDで削除 */
    void deleteById(Long id);
}
