package jp.yoshiaki.insuranceapp.training.day90.health;

import java.util.List;

/**
 * 書籍データアクセスのインターフェース。
 * 保存・全件取得に加え、ヘルスチェック用の isAvailable() を定義する。
 */
public interface BookRepository {

    /**
     * 書籍を保存する。
     *
     * @param book 保存する書籍
     * @return IDが採番された書籍
     */
    Book save(Book book);

    /**
     * 全件取得する。
     *
     * @return 書籍のリスト
     */
    List<Book> findAll();

    /**
     * データストアが利用可能かどうかを返す。
     * ヘルスチェック（BookStoreHealthIndicator）から呼ばれる。
     *
     * @return 利用可能なら true
     */
    boolean isAvailable();
}
