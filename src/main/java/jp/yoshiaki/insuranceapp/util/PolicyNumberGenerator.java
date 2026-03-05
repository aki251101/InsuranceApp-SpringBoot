// 配置：src/main/java/jp/yoshiaki/insuranceapp/util/PolicyNumberGenerator.java
package jp.yoshiaki.insuranceapp.util;

import jp.yoshiaki.insuranceapp.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 契約番号を自動で附番するユーティリティ。
 *
 * 契約番号の形式: P-{年度}-{連番4桁}
 *   例: P-2026-0001, P-2026-0002, ...
 *
 * ルール:
 *   - 年度は契約開始日の「西暦4桁」を使用する
 *   - 連番は年度ごとにリセットされる（0001 から開始）
 *   - DB に同じ年度の契約がなければ 0001 を附番する
 *   - DB に同じ年度の契約があれば「最大番号 + 1」を附番する
 *   - 連番の上限は 9999（4桁）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyNumberGenerator {

    private final PolicyRepository policyRepository;

    /**
     * 契約番号を自動生成する。
     *
     * @param startDate 契約開始日（年度の決定に使用）
     * @return 生成された契約番号（例: "P-2026-0001"）
     * @throws IllegalArgumentException 開始日が null の場合
     * @throws IllegalStateException    連番が 9999 を超えた場合
     */
    public String generate(LocalDate startDate) {
        // ① 引数チェック
        if (startDate == null) {
            throw new IllegalArgumentException("開始日が設定されていません");
        }

        // ② プレフィックスを組み立てる（例: "P-2026-"）
        int year = startDate.getYear();
        String prefix = String.format("P-%d-", year);

        // ③ 同じ年度で最大の契約番号を DB から取得する
        String maxNumber = policyRepository.findMaxPolicyNumberByPrefix(prefix);

        // ④ 次の連番を決定する
        int nextSequence;
        if (maxNumber == null) {
            // その年度の契約がまだ 0 件 → 0001 から開始
            nextSequence = 1;
        } else {
            // 最大番号の末尾（連番部分）を切り出して +1
            // 例: "P-2026-0042" → prefix="P-2026-" → 残り="0042" → 42 → 43
            String sequencePart = maxNumber.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                log.error("契約番号の連番部分を解析できません: maxNumber={}", maxNumber, e);
                throw new IllegalStateException("契約番号の形式が不正です: " + maxNumber);
            }
        }

        // ⑤ 上限チェック（4桁 = 最大 9999 件/年度）
        if (nextSequence > 9999) {
            throw new IllegalStateException(
                    String.format("年度 %d の契約番号が上限(9999)に達しました", year));
        }

        // ⑥ 契約番号を組み立てる（連番は0埋め4桁）
        String generated = String.format("%s%04d", prefix, nextSequence);
        log.info("契約番号を自動附番: {}", generated);

        return generated;
    }
}
