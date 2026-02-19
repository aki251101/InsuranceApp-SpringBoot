package jp.yoshiaki.insuranceapp.training.day80.order;

/**
 * 注文を表すドメインクラス。
 * 状態遷移ルール：PENDING → CONFIRMED または PENDING → FAILED
 */
public class Order {

    private final int id;
    private final String productName;
    private OrderStatus status;
    private final String idempotencyKey; // null の場合は冪等キーなし

    public Order(int id, String productName, String idempotencyKey) {
        this.id = id;
        this.productName = productName;
        this.status = OrderStatus.PENDING; // 作成直後は保留中
        this.idempotencyKey = idempotencyKey;
    }

    // ① PENDING → CONFIRMED への状態遷移
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "確定できるのは保留中の注文のみです（現在: " + status.toJapanese() + "）"
            );
        }
        this.status = OrderStatus.CONFIRMED;
    }

    // ② PENDING → FAILED への状態遷移
    public void fail() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "失敗にできるのは保留中の注文のみです（現在: " + status.toJapanese() + "）"
            );
        }
        this.status = OrderStatus.FAILED;
    }

    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    @Override
    public String toString() {
        String keyInfo = (idempotencyKey != null)
            ? "冪等キー=" + idempotencyKey.substring(0, 8) + "..."
            : "冪等キーなし";
        return String.format("[注文#%d] %s | 状態: %s | %s",
            id, productName, status.toJapanese(), keyInfo);
    }
}
