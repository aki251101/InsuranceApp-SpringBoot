package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI連携Service
 *
 * Entityの情報からプロンプト（AIへの指示文）を組み立て、
 * AiClient（interface）を通じてAI応答を取得する。
 *
 * 【重要】AI応答はDBに保存しない（画面表示のみ・揮発性）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiClient aiClient;

    public String summarizePolicy(Policy policy) {
        log.info("契約概要を要約: policyId={}", policy.getId());

        String prompt = String.format(
                "以下の損害保険契約の情報を、保険担当者向けに1〜2文で簡潔に要約してください。\n\n"
                        + "契約番号: %s\n"
                        + "契約者名: %s\n"
                        + "契約開始日: %s\n"
                        + "満期日: %s\n"
                        + "ステータス: %s\n\n"
                        + "要約:",
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getEffectiveStatus()
        );

        return aiClient.ask(prompt);
    }

    public String suggestNextActions(Accident accident) {
        log.info("事故の次アクション候補を生成: accidentId={}", accident.getId());

        String prompt = String.format(
                "以下の自動車保険の事故情報から、保険担当者が次に取るべきアクションを3つ、"
                        + "箇条書きで提案してください。\n\n"
                        + "事故受付日: %s\n"
                        + "事故場所: %s\n"
                        + "事故概要: %s\n"
                        + "現在のステータス: %s\n"
                        + "最終対応日: %s\n"
                        + "対応履歴メモ: %s\n\n"
                        + "次のアクション候補（3つ）:",
                accident.getOccurredAt(),
                accident.getPlace() != null ? accident.getPlace() : "不明",
                accident.getDescription() != null ? accident.getDescription() : "未入力",
                accident.getStatusLabel(),
                accident.getLastContactedAt() != null ? accident.getLastContactedAt() : "未対応",
                accident.getMemo() != null && !accident.getMemo().isBlank()
                        ? accident.getMemo() : "なし"
        );

        return aiClient.ask(prompt);
    }
}
