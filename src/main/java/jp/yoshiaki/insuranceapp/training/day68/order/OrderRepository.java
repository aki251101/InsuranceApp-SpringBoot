package jp.yoshiaki.insuranceapp.training.day68.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 注文リポジトリ。
 * JpaRepositoryを継承するだけで、基本的なCRUD操作が使える。
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 追加メソッドなし
}
