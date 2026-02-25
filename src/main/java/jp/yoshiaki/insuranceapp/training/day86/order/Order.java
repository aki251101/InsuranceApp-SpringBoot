package jp.yoshiaki.insuranceapp.training.day86.order;

import java.time.LocalDateTime;

/**
 * 注文ドメインモデル。
 * 1件の注文を表す（商品名・数量・注文日時を保持）。
 */
public class Order {

    private Long id;               // 注文ID（自動採番）
    private String itemName;       // 商品名
    private int quantity;          // 数量
    private LocalDateTime orderedAt; // 注文日時

    // ① コンストラクタ：ID・商品名・数量を受け取り、注文日時は自動で現在時刻を設定
    public Order(Long id, String itemName, int quantity) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.orderedAt = LocalDateTime.now(); // 注文時点の日時を自動記録
    }

    // ② getter のみ（注文データは作成後に変更しない想定）
    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
}
