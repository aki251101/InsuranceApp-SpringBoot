package jp.yoshiaki.insuranceapp.training.day72.book;

/**
 * 書籍エンティティ（ドメイン）。
 * REST API で扱う「リソース（Resource）」の実体。
 */
public class Book {

    private Long id;
    private String title;
    private String isbn;

    // ① コンストラクタ：新規作成時に必須フィールドを受け取る
    public Book(Long id, String title, String isbn) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
    }

    // ② 更新メソッド：PUT で情報を書き換えるときに使う
    public void update(String title, String isbn) {
        this.title = title;
        this.isbn = isbn;
    }

    // --- getter ---

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }
}
