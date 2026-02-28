package jp.yoshiaki.insuranceapp.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import jp.yoshiaki.insuranceapp.domain.Policy;

/**
 * Day65学習用：インメモリ実装。
 * - 404確認：存在しないIDで /policies/{id} を叩く
 * - 200確認：1 or 2 を叩く（例：/policies/1）
 */
@Component
public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<Long, Policy> store = new LinkedHashMap<>();

    public InMemoryPolicyRepository() {
        // 学習用のダミーデータ（2件）

        LocalDate start1 = LocalDate.of(2026, 1, 1);
        store.put(1L, new Policy(
                1L,
                101L,
                "山田太郎",
                "自動車保険",
                start1,
                12000,
                "P-0001",
                start1.plusYears(1),
                "有効"
        ));

        LocalDate start2 = LocalDate.of(2026, 2, 1);
        store.put(2L, new Policy(
                2L,
                102L,
                "佐藤花子",
                "火災保険",
                start2,
                8000,
                "P-0002",
                start2.plusYears(1),
                "有効"
        ));
    }

    @Override
    public Optional<Policy> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Policy> findAll() {
        return new ArrayList<>(store.values());
    }
}
