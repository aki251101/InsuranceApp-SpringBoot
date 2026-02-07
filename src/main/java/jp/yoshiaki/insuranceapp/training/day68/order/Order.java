package jp.yoshiaki.insuranceapp.training.day68.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 注文エンティティ。
 * 注文作成時に商品名を転記して保存する（商品が後から消えても注文記録は残る）。
 */
@Entity
@Table(name = "day68_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 注文時の商品名（Productから転記） */
    private String productName;

    /** 注文日時 */
    private LocalDateTime orderedAt;

    // ── JPA用のデフォルトコンストラクタ（必須） ──
    protected Order() {
    }

    // ── 注文作成用コンストラクタ ──
    public Order(Product product) {
        this.productName = product.getName();
        this.orderedAt = LocalDateTime.now();
    }

    // ── Getter ──

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
}
