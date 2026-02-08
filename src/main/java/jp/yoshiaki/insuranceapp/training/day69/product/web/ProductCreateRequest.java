package jp.yoshiaki.insuranceapp.training.day69.product.web;

/**
 * POST /api/day69/products のリクエストボディを受け取るDTO。
 * Controllerに直接フィールドを渡すのではなく、DTOで受け取ることで
 * 「何を受け取るか」を明示できる。
 */
public class ProductCreateRequest {

    private String name;
    private int price;

    // デフォルトコンストラクタ（JSONデシリアライズ用：Jacksonが使う）
    public ProductCreateRequest() {
    }

    public ProductCreateRequest(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
