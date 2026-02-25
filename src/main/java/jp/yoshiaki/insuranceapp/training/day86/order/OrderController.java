package jp.yoshiaki.insuranceapp.training.day86.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 注文管理のREST APIコントローラー。
 * エンドポイント: /api/day86/orders
 */
@RestController("day86OrderController") // Bean名を明示（他Dayとの衝突防止）
@RequestMapping("/api/day86/orders")    // このクラスの全エンドポイントの共通パス
public class OrderController {

    private final OrderService orderService;

    // ① コンストラクタインジェクション
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 注文を新規作成する。
     * POST /api/day86/orders
     * リクエストボディ例: {"itemName": "ノートPC", "quantity": 2}
     */
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Map<String, Object> request) {
        // ② @RequestBody でJSONをMapとして受け取る（簡易的な方法）
        String itemName = (String) request.get("itemName");
        int quantity = (int) request.get("quantity");

        Order created = orderService.create(itemName, quantity);

        // ③ 201 Created を返す（新規作成の成功を示すHTTPステータス）
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 全注文を取得する。
     * GET /api/day86/orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> findAll() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders); // 200 OK
    }

    /**
     * IDで注文を1件取得する。
     * GET /api/day86/orders/{id}
     * 見つからない場合は NotFoundException → 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable Long id) {
        // ④ @PathVariable でURLの{id}部分を取得
        Order order = orderService.findById(id);
        return ResponseEntity.ok(order); // 200 OK
    }
}
