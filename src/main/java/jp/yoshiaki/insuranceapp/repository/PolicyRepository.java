package jp.yoshiaki.insuranceapp.repository;

import java.util.List;
import java.util.Optional;

import jp.yoshiaki.insuranceapp.domain.Policy;

/**
 * Day65学習用：最小のRepository。
 * 実DB/JPAはまだ使わず、InMemory実装で Service/Advice の流れを確認する。
 */
public interface PolicyRepository {

    Optional<Policy> findById(Long id);

    List<Policy> findAll();
}
