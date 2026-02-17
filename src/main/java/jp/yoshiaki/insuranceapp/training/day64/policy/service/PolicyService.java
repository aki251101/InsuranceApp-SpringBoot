package jp.yoshiaki.insuranceapp.training.day64.policy.service;

import jp.yoshiaki.insuranceapp.training.day64.policy.domain.Policy;
import jp.yoshiaki.insuranceapp.training.day64.policy.repository.PolicyRepository;
import jp.yoshiaki.insuranceapp.training.day64.policy.web.dto.PolicyCreateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Profile("training")
@Service("day64PolicyService")
public class PolicyService {

    private final PolicyRepository repository;
    private final AtomicLong seq = new AtomicLong(0);

    // ★コンストラクタ注入（newしない）
    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }

    public Policy create(PolicyCreateRequest req) {
        long id = seq.incrementAndGet();
        LocalDate start = LocalDate.parse(req.startDate);
        Policy policy = new Policy(id, req.customerId, req.productName, start);
        return repository.save(policy);
    }

    public List<Policy> list() {
        return repository.findAll();
    }
}
