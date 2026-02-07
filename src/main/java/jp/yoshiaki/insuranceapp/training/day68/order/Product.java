package jp.yoshiaki.insuranceapp.training.day68.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 商品エンティティ。
 * 在庫の増減ロジックをドメインオブジェクト自身に持たせる設計。
 */
@Entity
@Table(name = "day68_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品名 */
    private String name;

    /** 在庫数（0以上を保証する） */
    private int stock;

    // ── JPA用のデフォルトコンストラクタ（必須） ──
    protected Product() {
    }

    // ── 登録用コンストラクタ ──
    public Product(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    // ── 業務ロジック ──

    /**
     * 在庫を1つ減らす。
     * 在庫が0の場合は IllegalStateException をスローする。
     * → RuntimeException なので @Transactional の自動rollback対象。
     */
    public void decreaseStock() {
        if (this.stock <= 0) {
            throw new IllegalStateException("在庫切れです: " + this.name);
        }
        this.stock--;
    }

    // ── Getter ──

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }
}
