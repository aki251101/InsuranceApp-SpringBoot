package jp.yoshiaki.insuranceapp.training.day64.policy.web;

import jp.yoshiaki.insuranceapp.training.day64.policy.domain.Policy;
import jp.yoshiaki.insuranceapp.training.day64.policy.service.PolicyService;
import jp.yoshiaki.insuranceapp.training.day64.policy.web.dto.PolicyCreateRequest;
import jp.yoshiaki.insuranceapp.training.day64.policy.web.dto.PolicyResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("training")
@RestController("day64PolicyController")
@RequestMapping("/training/day64/policies")
public class PolicyController {

    private final PolicyService service;

    // ★コンストラクタ注入
    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    public PolicyResponse create(@RequestBody PolicyCreateRequest req) {
        Policy saved = service.create(req);
        return new PolicyResponse(
                saved.getId(),
                saved.getCustomerId(),
                saved.getProductName(),
                saved.getStartDate().toString()
        );
    }

    @GetMapping
    public List<PolicyResponse> list() {
        return service.list().stream()
                .map(p -> new PolicyResponse(p.getId(), p.getCustomerId(), p.getProductName(), p.getStartDate().toString()))
                .toList();
    }
}
