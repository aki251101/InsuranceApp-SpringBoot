package jp.yoshiaki.insuranceapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.yoshiaki.insuranceapp.domain.Policy;
import jp.yoshiaki.insuranceapp.domain.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.domain.exception.ValidationException;
import jp.yoshiaki.insuranceapp.repository.PolicyRepository;

/**
 * Day65: 例外集約（@RestControllerAdvice）とセットで使う Service。
 *
 * Controller は成功レスポンスに集中し、Service は業務判断で DomainException を投げる。
 * - NotFoundException -> ApiExceptionHandler で 404
 * - ValidationException -> ApiExceptionHandler で 400
 */
@Service
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * 単体取得：存在しないIDは NotFoundException（→404）にする
     */
    public Policy findById(Long id) {
        // テスト用：id が 0 以下なら ValidationException
        if (id <= 0) {
            throw new ValidationException("id must be positive: id=" + id);
        }

        Policy policy = policyRepository.findById(id).orElse(null);

        if (policy == null) {
            throw new NotFoundException("policy not found: id=" + id);
        }

        return policy;
    }

    /**
     * 一覧取得（任意：動作確認に便利）
     */
    public List<Policy> list() {
        return policyRepository.findAll();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be a positive number");
        }
    }
}
