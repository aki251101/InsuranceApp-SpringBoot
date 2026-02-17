package jp.yoshiaki.insuranceapp.training.day63.policy.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import jp.yoshiaki.insuranceapp.training.day63.policy.domain.Policy;
import jp.yoshiaki.insuranceapp.training.day63.policy.repository.PolicyRepository;
import jp.yoshiaki.insuranceapp.training.day63.policy.web.dto.PolicyCreateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("training")
@Service("day63PolicyService")
public class PolicyService {

    private final PolicyRepository repo;
    private final AtomicLong idGen = new AtomicLong(0);

    public PolicyService(PolicyRepository repo) {
        this.repo = repo;
    }

    public Policy create(PolicyCreateRequest req) {
        long id = idGen.incrementAndGet();
        Policy policy = new Policy(id, req.getCustomerName(), req.getStartDate(), req.getPremium());
        return repo.save(policy);
    }

    public List<Policy> list() {
        return repo.findAll();
    }
}
