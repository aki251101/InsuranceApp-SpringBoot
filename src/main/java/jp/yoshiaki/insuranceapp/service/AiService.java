package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.repository.AccidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    private final AccidentRepository accidentRepository;

    public String summarizePolicy(Policy policy) {
        log.info("契約概要を要約: policyId={}", policy.getId());

        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, policy.getEndDate());
        String attention = policy.isAttentionRequired() ? "要注意" : "通常";
        String renewable = policy.isRenewable() ? "更新可" : "更新不可";
        long accidentCount = accidentRepository.countByPolicyId(policy.getId());

        String prompt = String.format(
                "以下の損害保険契約情報を、保険担当者向けに実務で使える形で要約してください。\n"
                        + "出力は必ず次の2行のみで返してください（見出しも固定）。\n"
                        + "1行目: 現状　： 契約の状態・満期までの状況・事故履歴の要点\n"
                        + "2行目: 注意点： 優先して確認/対応すべき事項（なければ『特記事項なし』）\n"
                        + "制約: 各行は60文字以内、箇条書き禁止、前置き/補足/改行追加禁止。\n\n"
                        + "契約番号: %s\n"
                        + "契約者名: %s\n"
                        + "契約開始日: %s\n"
                        + "満期日: %s\n"
                        + "満期まで残日数: %d日\n"
                        + "ステータス: %s\n"
                        + "要注意判定: %s\n"
                        + "更新可否: %s\n"
                        + "過去事故件数: %d件\n\n"
                        + "要約:",
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getStartDate(),
                policy.getEndDate(),
                daysUntilExpiry,
                policy.getEffectiveStatus(),
                attention,
                renewable,
                accidentCount
        );

        String raw = aiClient.ask(prompt);
        return normalizePolicySummary(raw, policy, daysUntilExpiry, attention, renewable, accidentCount);
    }

    private String normalizePolicySummary(
            String raw,
            Policy policy,
            long daysUntilExpiry,
            String attention,
            String renewable,
            long accidentCount) {

        if (raw == null || raw.isBlank()) {
            return buildFallbackSummary(policy, daysUntilExpiry, attention, renewable, accidentCount);
        }

        String[] lines = raw.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);

        String current = null;
        String caution = null;
        for (String line : lines) {
            if (current == null && line.startsWith("現状:")) {
                current = line.substring("現状:".length()).trim();
                continue;
            }
            if (caution == null && line.startsWith("注意点:")) {
                caution = line.substring("注意点:".length()).trim();
            }
        }

        if (current == null || current.isBlank() || caution == null || caution.isBlank()) {
            return buildFallbackSummary(policy, daysUntilExpiry, attention, renewable, accidentCount);
        }

        return "現状: " + current + "\n注意点: " + caution;
    }

    private String buildFallbackSummary(
            Policy policy,
            long daysUntilExpiry,
            String attention,
            String renewable,
            long accidentCount) {

        String current = String.format(
                "%s。満期まで%d日、%s、事故履歴%d件。",
                policy.getEffectiveStatus(), daysUntilExpiry, renewable, accidentCount
        );

        String caution = "要注意".equals(attention)
                ? "満期が近いため更新意向と手続き状況を優先確認。"
                : "特記事項なし";

        return "現状: " + current + "\n注意点: " + caution;
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
