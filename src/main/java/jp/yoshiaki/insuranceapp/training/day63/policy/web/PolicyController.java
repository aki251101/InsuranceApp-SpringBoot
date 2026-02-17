package jp.yoshiaki.insuranceapp.training.day63.policy.web;

import java.util.List;

import jp.yoshiaki.insuranceapp.training.day63.policy.domain.Policy;
import jp.yoshiaki.insuranceapp.training.day63.policy.service.PolicyService;
import jp.yoshiaki.insuranceapp.training.day63.policy.web.dto.PolicyCreateRequest;
import jp.yoshiaki.insuranceapp.training.day63.policy.web.dto.PolicyResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Boot3: jakarta.validation.Valid
// Boot2: javax.validation.Valid
import jakarta.validation.Valid;

@Profile("training")
@RestController("day63PolicyController")
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse create(@Valid @RequestBody PolicyCreateRequest req) {
        Policy created = service.create(req);
        return PolicyResponse.from(created);
    }

    @GetMapping
    public List<PolicyResponse> list() {
        return service.list().stream().map(PolicyResponse::from).toList();
    }
}
