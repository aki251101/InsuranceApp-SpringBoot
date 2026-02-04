package jp.yoshiaki.insuranceapp.web.controller;

import jp.yoshiaki.insuranceapp.domain.Policy;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import jp.yoshiaki.insuranceapp.web.dto.PolicyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long id) {
        Policy policy = policyService.findById(id);
        return ResponseEntity.ok(new PolicyResponse(policy));
    }
}