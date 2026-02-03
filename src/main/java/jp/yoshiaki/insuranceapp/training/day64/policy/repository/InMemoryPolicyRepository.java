package jp.yoshiaki.insuranceapp.training.day64.policy.repository;

import jp.yoshiaki.insuranceapp.training.day64.policy.domain.Policy;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository("day64InMemoryPolicyRepository")
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
