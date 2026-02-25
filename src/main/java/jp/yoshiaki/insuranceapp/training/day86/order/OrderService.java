package jp.yoshiaki.insuranceapp.training.day86.order;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 注文の業務ロジックを担当するサービスクラス。
 * Controllerからの依頼を受け、Repositoryにデータ操作を委譲する。
 */
@Service("day86OrderService") // Bean名を明示（他Dayとの衝突防止）
public class OrderService {

    private final OrderRepository orderRepository;

    // ① コンストラクタインジェクション：Springが自動でInMemoryOrderRepositoryを注入する
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 注文を新規作成する。
     * @param itemName 商品名
     * @param quantity 数量
     * @return 作成された注文
     */
    public Order create(String itemName, int quantity) {
        // IDはnullで渡す → Repository側で自動採番される
        Order order = new Order(null, itemName, quantity);
        return orderRepository.save(order);
    }

    /**
     * IDで注文を1件取得する。見つからない場合はNotFoundExceptionをスローする。
     * @param id 注文ID
     * @return 注文
     * @throws NotFoundException IDに該当する注文が存在しない場合
     */
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("注文ID " + id + " が見つかりません"));
    }

    /**
     * 全注文を取得する。
     * @return 全注文のリスト
     */
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
