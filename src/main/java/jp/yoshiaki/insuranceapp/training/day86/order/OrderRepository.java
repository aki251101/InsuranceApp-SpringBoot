package jp.yoshiaki.insuranceapp.training.day86.order;

import java.util.List;
import java.util.Optional;

/**
 * 注文リポジトリのインターフェース（保存窓口の定義）。
 * 「どうやって保存するか」は実装クラスに任せる。
 * これにより、InMemory → DB などの差し替えが容易になる。
 */
public interface OrderRepository {

    /** 注文を保存し、保存後の注文を返す */
    Order save(Order order);

    /** IDで注文を検索する（見つからない場合はOptional.empty()） */
    Optional<Order> findById(Long id);

    /** 全注文を取得する */
    List<Order> findAll();
}
