package jp.yoshiaki.insuranceapp.training.day69.product.repository;

import jp.yoshiaki.insuranceapp.training.day69.product.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * 商品の保存・取得の契約（interface）。
 * テスト時はこのinterfaceの実装をモックに差し替える。
 */
public interface ProductRepository {

    /** 全商品を取得する */
    List<Product> findAll();

    /** IDで商品を検索する（見つからなければ空のOptional） */
    Optional<Product> findById(Long id);

    /** 商品を保存してIDを採番済みの状態で返す */
    Product save(Product product);
}
