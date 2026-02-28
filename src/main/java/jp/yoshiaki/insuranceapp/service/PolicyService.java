package jp.yoshiaki.insuranceapp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.client.CalendarClient;
import jp.yoshiaki.insuranceapp.domain.Policy;
import jp.yoshiaki.insuranceapp.domain.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.domain.exception.ValidationException;
import jp.yoshiaki.insuranceapp.repository.PolicyRepository;

/**
 * Day65: 例外集約（@RestControllerAdvice）とセットで使う Service。
 *
 * Day89: 外部連携の境界（CalendarClient / AiClient）を注入し、
 *        Stub でも本番でも動線が通る形にする。
 */
@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyRepository policyRepository;
    private final CalendarClient calendarClient;
    private final AiClient aiClient;

    public PolicyService(PolicyRepository policyRepository, CalendarClient calendarClient, AiClient aiClient) {
        this.policyRepository = policyRepository;
        this.calendarClient = calendarClient;
        this.aiClient = aiClient;
    }

    /**
     * 単体取得：存在しないIDは NotFoundException（→404）にする
     */
    public Policy findById(Long id) {
        if (id == null || id <= 0) {
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

    /**
     * Day89: 契約情報のAI要約を生成する（Stubなら固定文が返る）
     */
    public String summarizePolicy(Long id) {
        // 学習ログ（動線確認）
        log.info("AI契約要約生成: id={}", id);

        Policy policy = findById(id);
        return aiClient.summarizePolicy(policy);
    }

    /**
     * Day89: 満期日リマインドイベントをカレンダーに登録する（Stubなら固定IDが返る）
     */
    public String createExpiryReminderEvent(Long id) {
        // 学習ログ（動線確認）
        log.info("カレンダー登録トグル: id={}", id);

        Policy policy = findById(id);
        return calendarClient.createEvent(policy);
    }

    /**
     * Day89: 登録済みイベントを削除する（動作確認用）
     */
    public void deleteReminderEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new ValidationException("eventId must not be blank");
        }
        calendarClient.deleteEvent(eventId);
    }
}
