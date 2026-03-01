package jp.yoshiaki.insuranceapp.training.day90.health;

/**
 * 書籍ドメインモデル。
 * 書店の在庫管理で扱う1冊分の情報を表す。
 */
public class Book {

    private final Long id;
    private final String title;
    private final String author;
    private final int stock;

    // ── コンストラクタ ──
    public Book(Long id, String title, String author, int stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.stock = stock;
    }

    // ── getter ──
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getStock() {
        return stock;
    }

    // ── 表示用 ──
    @Override
    public String toString() {
        return String.format("Book{id=%d, title='%s', author='%s', stock=%d}",
                id, title, author, stock);
    }
}
