package jp.yoshiaki.insuranceapp.training.day68.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 注文API。
 * OrderServiceの @Transactional の効果を確認するためのコントローラ。
 */
@RestController("day68OrderController")
@RequestMapping("/api/day68/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 注文を作成する。
     * 在庫が足りない場合は 400 Bad Request を返す。
     *
     * 使い方: POST /api/day68/orders?productId=1
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@RequestParam Long productId) {
        return orderService.placeOrder(productId);
    }

    /**
     * 注文一覧を取得する。
     */
    @GetMapping
    public List<Order> list() {
        return orderService.findAllOrders();
    }

    // ── 例外ハンドラ（このController内の例外をキャッチ） ──

    /**
     * 商品が見つからない場合のエラーレスポンス。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * 在庫切れなどの業務エラーのレスポンス。
     * IllegalStateException は RuntimeException のサブクラスなので、
     * @Transactional による自動rollbackが発動した「後」にここに到達する。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBusinessError(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
