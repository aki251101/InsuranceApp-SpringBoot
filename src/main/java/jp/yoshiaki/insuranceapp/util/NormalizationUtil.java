// 配置：src/main/java/jp/yoshiaki/insuranceapp/util/NormalizationUtil.java
package jp.yoshiaki.insuranceapp.util;

import java.text.Normalizer;

/**
 * 文字列の正規化ユーティリティ。
 *
 * 全角/半角の揺れを統一するために使う。
 * たとえば「Ｐ−２０２６」を「P-2026」に変換する（NFKC正規化）。
 *
 * NFKC正規化とは：
 *   Unicode の互換分解 + 正規合成を行う変換のこと。
 *   全角英数字 → 半角、半角カナ → 全角カナ、など「見た目が同じ文字」を統一する。
 */
public class NormalizationUtil {

    /**
     * 契約番号を正規化する（全角→半角 + 大文字化 + 前後の空白除去）。
     *
     * @param policyNumber 入力された契約番号
     * @return 正規化済みの契約番号（null/空白の場合は空文字）
     */
    public static String normalizePolicyNumber(String policyNumber) {
        if (policyNumber == null || policyNumber.isBlank()) {
            return "";
        }
        // NFKC で全角→半角変換し、さらに大文字に揃える
        String normalized = Normalizer.normalize(policyNumber.trim(), Normalizer.Form.NFKC);
        return normalized.toUpperCase();
    }

    /**
     * 契約者名を正規化する（全角/半角統一 + 前後の空白除去）。
     *
     * @param customerName 入力された契約者名
     * @return 正規化済みの契約者名（null/空白の場合は空文字）
     */
    public static String normalizeCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return "";
        }
        return Normalizer.normalize(customerName.trim(), Normalizer.Form.NFKC);
    }

    /**
     * 検索キーワードを正規化する（全角/半角統一 + 前後の空白除去）。
     *
     * @param keyword 入力された検索キーワード
     * @return 正規化済みのキーワード（null/空白の場合は空文字）
     */
    public static String normalizeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }
        return Normalizer.normalize(keyword.trim(), Normalizer.Form.NFKC);
    }
}
