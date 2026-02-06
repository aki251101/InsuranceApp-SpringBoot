package jp.yoshiaki.insuranceapp.training.day67.book;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 書籍Entity
 * DBテーブル「books」の1行を表すクラス
 */
@Entity
@Table(name = "day67_books")
public class Book {

    // ========== フィールド ==========

    /** 書籍ID（主キー、自動採番） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 書名 */
    private String title;

    /** 著者 */
    private String author;

    /** 価格（円） */
    private Integer price;

    // ========== コンストラクタ ==========

    /** JPA用デフォルトコンストラクタ（必須） */
    public Book() {
    }

    /** 新規作成用コンストラクタ（IDなし） */
    public Book(String title, String author, Integer price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                '}';
    }
}
