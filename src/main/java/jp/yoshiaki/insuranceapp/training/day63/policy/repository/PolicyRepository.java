package jp.yoshiaki.insuranceapp.training.day63.policy.repository;

import jp.yoshiaki.insuranceapp.training.day63.policy.domain.Policy;

import java.util.List;

public interface PolicyRepository {
    Policy save(Policy policy);
    List<Policy> findAll();
}
