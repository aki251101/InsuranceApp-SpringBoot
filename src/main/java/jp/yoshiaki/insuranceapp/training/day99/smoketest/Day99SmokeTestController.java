package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Day99 スモークテストController
 *
 * 役割：
 * - /api/day99/health → ヘルスチェック（DB＋外部APIの疎通確認）
 * - /api/day99/products → 商品CRUD（基本的なAPI疎通）
 * - /api/day99/smoke-test → スモークテスト一括実行（全エンドポイントの疎通をまとめて確認）
 * - /api/day99/fault/{type}/enable|disable → 障害シミュレーションの制御
 */
@RestController("day99SmokeTestController")
@RequestMapping("/api/day99")
public class Day99SmokeTestController {

    private static final Logger log = LoggerFactory.getLogger(Day99SmokeTestController.class);

    private final ProductService productService;
    private final HealthCheckService healthCheckService;
    private final FaultSimulator faultSimulator;

    // コンストラクタインジェクション
    public Day99SmokeTestController(ProductService productService,
                                     HealthCheckService healthCheckService,
                                     FaultSimulator faultSimulator) {
        this.productService = productService;
        this.healthCheckService = healthCheckService;
        this.faultSimulator = faultSimulator;
    }

    // ========== ヘルスチェック ==========

    /**
     * ヘルスチェックエンドポイント
     * 全コンポーネントの生死を返す
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResult> health() {
        log.info("ヘルスチェック開始");
        HealthResult result = healthCheckService.checkAll();

        // ① ステータスに応じてHTTPステータスを変える
        if ("UP".equals(result.getStatus())) {
            return ResponseEntity.ok(result);                      // 200 OK
        } else {
            return ResponseEntity.status(503).body(result);        // 503 Service Unavailable
        }
    }

    // ========== 商品CRUD ==========

    /**
     * 商品一覧を取得する
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> listProducts() {
        log.debug("商品一覧取得");
        try {
            List<Product> products = productService.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("商品一覧取得失敗: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 商品を登録する
     * リクエストボディ例: {"name": "自賠責保険", "price": 12000}
     */
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> body) {
        log.info("商品登録: body={}", body);
        try {
            String name = (String) body.get("name");
            int price = ((Number) body.get("price")).intValue(); // ② NumberでキャストしてからintValueで安全変換
            Product created = productService.create(name, price);
            return ResponseEntity.status(201).body(created);       // 201 Created
        } catch (Exception e) {
            log.error("商品登録失敗: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 商品詳細を取得する
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        log.debug("商品詳細取得: id={}", id);
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            log.warn("商品が見つかりません: id={}", id);
            return ResponseEntity.status(404).body(null);          // 404 Not Found
        } catch (Exception e) {
            log.error("商品詳細取得失敗: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // ========== スモークテスト一括実行 ==========

    /**
     * スモークテスト一括実行
     * ヘルスチェック → 商品登録 → 商品一覧 → 商品詳細 を順番に実行し、
     * 全ての結果をまとめてSmokeTestResultとして返す。
     */
    @GetMapping("/smoke-test")
    public ResponseEntity<SmokeTestResult> runSmokeTest() {
        log.info("===== スモークテスト開始 =====");
        SmokeTestResult smokeResult = new SmokeTestResult();

        // ③ テスト1: ヘルスチェック
        try {
            HealthResult health = healthCheckService.checkAll();
            boolean healthOk = "UP".equals(health.getStatus());
            smokeResult.addResult(
                    "ヘルスチェック",
                    healthOk,
                    healthOk ? "全コンポーネントUP" : "一部コンポーネントがDOWN: " + health.getComponents()
            );
        } catch (Exception e) {
            smokeResult.addResult("ヘルスチェック", false, "例外発生: " + e.getMessage());
            log.error("スモーク: ヘルスチェック失敗", e);
        }

        // ④ テスト2: 商品登録（書き込み疎通）
        Product created = null;
        try {
            created = productService.create("スモークテスト商品", 9999);
            smokeResult.addResult(
                    "商品登録（POST）",
                    true,
                    "登録成功: id=" + created.getId()
            );
        } catch (Exception e) {
            smokeResult.addResult("商品登録（POST）", false, "例外発生: " + e.getMessage());
            log.error("スモーク: 商品登録失敗", e);
        }

        // ⑤ テスト3: 商品一覧取得（読み取り疎通）
        try {
            List<Product> products = productService.findAll();
            smokeResult.addResult(
                    "商品一覧取得（GET）",
                    true,
                    "取得成功: " + products.size() + "件"
            );
        } catch (Exception e) {
            smokeResult.addResult("商品一覧取得（GET）", false, "例外発生: " + e.getMessage());
            log.error("スモーク: 商品一覧取得失敗", e);
        }

        // ⑥ テスト4: 商品詳細取得（パスパラメータ疎通）
        if (created != null) {
            try {
                Product found = productService.findById(created.getId());
                smokeResult.addResult(
                        "商品詳細取得（GET/{id}）",
                        true,
                        "取得成功: " + found.getName()
                );
            } catch (Exception e) {
                smokeResult.addResult("商品詳細取得（GET/{id}）", false, "例外発生: " + e.getMessage());
                log.error("スモーク: 商品詳細取得失敗", e);
            }
        } else {
            smokeResult.addResult("商品詳細取得（GET/{id}）", false, "前のテスト（商品登録）が失敗したためスキップ");
        }

        // ⑦ 全体結果を計算
        smokeResult.calculateOverall();

        log.info("===== スモークテスト完了: {} =====", smokeResult.getOverallResult());
        return ResponseEntity.ok(smokeResult);
    }

    // ========== 障害シミュレーション制御 ==========

    /**
     * 障害シミュレーションを有効にする
     * @param type "db" or "external"
     */
    @PostMapping("/fault/{type}/enable")
    public ResponseEntity<Map<String, String>> enableFault(@PathVariable String type) {
        switch (type) {
            case "db" -> {
                faultSimulator.enableDbFault();
                log.warn("【障害シミュレーション】DB障害を有効化しました");
            }
            case "external" -> {
                faultSimulator.enableExternalFault();
                log.warn("【障害シミュレーション】外部API障害を有効化しました");
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "不明な障害タイプ: " + type + "（'db' or 'external' を指定してください）"));
            }
        }
        return ResponseEntity.ok(Map.of("message", type + " 障害を有効化しました"));
    }

    /**
     * 障害シミュレーションを無効にする
     * @param type "db" or "external"
     */
    @PostMapping("/fault/{type}/disable")
    public ResponseEntity<Map<String, String>> disableFault(@PathVariable String type) {
        switch (type) {
            case "db" -> {
                faultSimulator.disableDbFault();
                log.info("【障害シミュレーション】DB障害を無効化しました");
            }
            case "external" -> {
                faultSimulator.disableExternalFault();
                log.info("【障害シミュレーション】外部API障害を無効化しました");
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "不明な障害タイプ: " + type));
            }
        }
        return ResponseEntity.ok(Map.of("message", type + " 障害を無効化しました"));
    }

    /**
     * 全障害シミュレーションをリセットする
     */
    @PostMapping("/fault/reset")
    public ResponseEntity<Map<String, String>> resetFaults() {
        faultSimulator.resetAll();
        log.info("【障害シミュレーション】全障害をリセットしました");
        return ResponseEntity.ok(Map.of("message", "全障害をリセットしました"));
    }
}
