package jp.yoshiaki.insuranceapp.training.day63.policy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.yoshiaki.insuranceapp.training.day63.policy.domain.Policy;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<Long, Policy> store = new LinkedHashMap<>();

    @Override
    public Policy save(Policy policy) {
        store.put(policy.getId(), policy);
        return policy;
    }

    @Override
    public List<Policy> findAll() {
        return new ArrayList<>(store.values());
    }
}
