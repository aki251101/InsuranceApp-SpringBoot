package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.domain.accident.Accident;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * AIによる「次アクション候補」生成サービス（業務支援）
 * - 外部AIの呼び出し責務は AiClient に委譲（本番: 実API / 開発: Stub）
 * - この層は "プロンプト" と "業務入力の整形" に集中
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiClient aiClient;

    public String suggestNextActions(Accident accident) {
        String prompt = buildPrompt(accident);
        log.info("AI次アクション候補生成: id={}", accident.getId());
        return aiClient.ask(prompt);
    }

    private String buildPrompt(Accident accident) {
        var dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String occurredAt = (accident.getOccurredAt() == null) ? "未入力" : dtf.format(accident.getOccurredAt());
        String lastContactedAt = (accident.getLastContactedAt() == null) ? "未入力" : dtf.format(accident.getLastContactedAt());
        String place = safe(accident.getPlace());
        String description = safe(accident.getDescription());
        String memo = safe(accident.getMemo());
        String statusLabel = safe(accident.getStatusLabel());

        // 本番運用を前提に、出力フォーマット（箇条書き、具体タスク、優先度）を固定化
        return """
            あなたは損保の事故受付担当の業務支援AIです。
            次の事故情報を読み、担当者が「次にやるべきアクション」を優先度順に3〜5個提案してください。
            出力は日本語、箇条書きで、各項目に「理由」と「具体的な一言トーク例」を添えてください。

            【事故情報】
            事故ID: %d
            ステータス: %s
            事故日時: %s
            場所: %s
            概要: %s
            最終連絡: %s
            メモ: %s
            """.formatted(accident.getId(), statusLabel, occurredAt, place, description, lastContactedAt, memo);
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "未入力" : v.trim();
    }
}
