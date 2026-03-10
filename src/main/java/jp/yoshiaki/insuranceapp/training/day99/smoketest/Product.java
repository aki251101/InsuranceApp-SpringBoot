package jp.yoshiaki.insuranceapp.training.day99.smoketest;

/**
 * 商品ドメインクラス
 * スモークテストの疎通確認対象となるCRUDリソース
 */
public class Product {

    private Long id;
    private String name;
    private int price;

    // コンストラクタ
    public Product(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // --- getter / setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}
