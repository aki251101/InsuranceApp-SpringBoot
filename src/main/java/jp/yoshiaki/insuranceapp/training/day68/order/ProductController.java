package jp.yoshiaki.insuranceapp.training.day68.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 商品API。
 * 注文のテストデータ（商品）を登録・確認するためのコントローラ。
 */
@RestController("day68ProductController")
@RequestMapping("/api/day68/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 商品を登録する。
     * リクエスト例: {"name": "ボールペン", "stock": 3}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int stock = (int) body.get("stock");
        Product product = new Product(name, stock);
        return productRepository.save(product);
    }

    /**
     * 商品一覧を取得する（在庫数の確認用）。
     */
    @GetMapping
    public List<Product> list() {
        return productRepository.findAll();
    }
}
