package jp.yoshiaki.insuranceapp.training.day64.policy.repository;

import jp.yoshiaki.insuranceapp.training.day64.policy.domain.Policy;

import java.util.List;

public interface PolicyRepository {
    Policy save(Policy policy);
    List<Policy> findAll();
}
