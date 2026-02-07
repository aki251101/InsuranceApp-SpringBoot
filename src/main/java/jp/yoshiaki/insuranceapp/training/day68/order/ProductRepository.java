package jp.yoshiaki.insuranceapp.training.day68.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品リポジトリ。
 * JpaRepositoryを継承するだけで、基本的なCRUD操作が使える。
 * Spring Data JPAが実行時に実装クラスを自動生成する。
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 追加メソッドなし（findById, findAll, save, deleteById は JpaRepository が提供）
}
