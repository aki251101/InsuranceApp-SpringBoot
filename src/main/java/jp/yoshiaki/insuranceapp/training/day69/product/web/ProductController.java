package jp.yoshiaki.insuranceapp.training.day69.product.web;

import jp.yoshiaki.insuranceapp.training.day69.product.domain.Product;
import jp.yoshiaki.insuranceapp.training.day69.product.exception.ProductNotFoundException;
import jp.yoshiaki.insuranceapp.training.day69.product.service.ProductService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品のREST APIエンドポイント。
 * HTTPリクエストを受け取り、Serviceに処理を委譲してJSONレスポンスを返す。
 *
 * MockMvcテストでは、このクラスが「テスト対象」になる。
 */
@Profile("training")
@RestController("day69ProductController")
@RequestMapping("/api/day69/products")
public class ProductController {

    private final ProductService service;

    // ① コンストラクタインジェクション
    public ProductController(ProductService service) {
        this.service = service;
    }

    // ② 全商品一覧を取得
    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    // ③ IDで商品を1件取得
    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    // ④ 新規商品を登録
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductCreateRequest request) {
        Product created = service.create(request.getName(), request.getPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ⑤ ProductNotFoundExceptionが発生したら404を返す
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ProductNotFoundException ex) {
        Map<String, String> error = Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
