package jp.yoshiaki.insuranceapp.web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.yoshiaki.insuranceapp.domain.Policy;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import jp.yoshiaki.insuranceapp.web.dto.PolicyResponse;

/**
 * Policy（契約）API
 *
 * 既存の /policies/{id} に加え、Day89の「Client境界（CalendarClient / AiClient）」動作確認用に
 * 以下のエンドポイントを追加する。
 *
 * - GET  /policies/{id}/summary         : AiClient による契約要約（stubなら固定文）
 * - POST /policies/{id}/calendar-event  : CalendarClient による満期リマインド登録（stubなら固定ID）
 * - DELETE /policies/calendar-event?eventId=... : 登録イベント削除（動作確認用）
 */
@RestController
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * 契約の単体取得（既存）
     */
    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long id) {
        Policy policy = policyService.findById(id);
        return ResponseEntity.ok(new PolicyResponse(policy));
    }

    /**
     * Day89: 契約要約（AI）
     */
    @GetMapping(value = "/{id}/summary", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPolicySummary(@PathVariable Long id) {
        return policyService.summarizePolicy(id);
    }

    /**
     * Day89: 満期リマインドのカレンダー登録
     */
    @PostMapping(value = "/{id}/calendar-event", produces = MediaType.TEXT_PLAIN_VALUE)
    public String createExpiryReminderEvent(@PathVariable Long id) {
        return policyService.createExpiryReminderEvent(id);
    }

    /**
     * Day89: 登録済みイベントの削除（動作確認用）
     */
    @DeleteMapping(value = "/calendar-event", produces = MediaType.TEXT_PLAIN_VALUE)
    public String deleteCalendarEvent(@RequestParam String eventId) {
        policyService.deleteReminderEvent(eventId);
        return "deleted: " + eventId;
    }
}
