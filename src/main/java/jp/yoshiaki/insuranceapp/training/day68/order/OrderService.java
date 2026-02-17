package jp.yoshiaki.insuranceapp.training.day68.order;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 注文サービス。
 * @Transactional の効果を体験するための中心クラス。
 *
 * ポイント：
 * - placeOrder() 内の「在庫減少」と「注文保存」は1トランザクション
 * - 途中で例外が飛ぶと、両方とも自動でrollback（巻き戻し）される
 * - readOnly = true は「このメソッドは読み取りだけ」とDBに伝える最適化
 */
@Profile("training")
@Service("day68OrderService")
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // ── コンストラクタ注入（Day64で学んだパターン） ──
    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 注文を作成する（在庫減少 ＋ 注文保存 = 1トランザクション）。
     *
     * ① productRepository.findById() で商品を取得
     * ② product.decreaseStock() で在庫を1つ減らす（在庫0なら例外）
     * ③ orderRepository.save() で注文を保存
     *
     * ②で例外が飛ぶと、③は実行されず、①以降の変更もrollbackされる。
     * ③の後に例外が飛んでも、①②③すべてrollbackされる。
     */
    @Transactional  // ← これが「まとまり保証」のスイッチ
    public Order placeOrder(Long productId) {
        // ① 商品を取得（見つからなければ例外）
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "商品が見つかりません: id=" + productId));

        // ② 在庫を1つ減らす（在庫0なら IllegalStateException）
        // ※ JPAの「ダーティチェック」により、product のフィールドを変更するだけで
        //   トランザクション終了時に自動でUPDATE SQLが発行される
        product.decreaseStock();

        // ③ 注文を保存
        Order order = new Order(product);
        return orderRepository.save(order);
        // ← メソッド正常終了 → commit（全変更をDBに確定）
    }

    /**
     * 注文一覧を取得する（読み取り専用）。
     * readOnly = true を付けると：
     * - DBに「変更しません」と伝えるので、DB側の最適化が効く
     * - うっかり save() を呼んでも反映されない（安全装置）
     */
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
}
