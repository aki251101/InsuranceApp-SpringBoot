package jp.yoshiaki.insuranceapp.util;

import java.text.Normalizer;

/**
 * 文字列正規化ユーティリティ（NormalizationUtil）
 *
 * 損保アプリで入力される文字列の「表記ゆれ」を統一するための static メソッド群。
 *
 * 【なぜ必要か？】
 *   ユーザーが全角で「Ｐ−２０２６−０００１」と入力しても、
 *   DB上は半角の「P-2026-0001」として保存したい。
 *   同じ契約番号なのに全角/半角で別物として扱われると、検索漏れや重複登録が起きる。
 *
 * 【NFKC（互換分解＋正準合成）とは？】
 *   Unicode の正規化形式の1つ。以下の変換を行う：
 *     - 全角英数字 → 半角英数字（例：Ａ→A、１→1）
 *     - 全角カタカナ → 半角カタカナ にはしない（カタカナは全角を維持）
 *     - 互換文字 → 標準文字（例：㈱→(株)、㍻→平成）
 *     - 結合文字 → 合成文字（例：が → が）
 *
 * 【設計判断】
 *   - static メソッドにした理由：状態を持たないユーティリティなので、インスタンス化不要
 *   - Service 層から呼ぶ（Controller 層では呼ばない）
 */
public class NormalizationUtil {

    /**
     * 契約番号の正規化
     *
     * 処理順序：
     *   1. null / 空白チェック → 空文字を返す
     *   2. trim()：前後の空白を除去
     *   3. NFKC正規化：全角英数字 → 半角英数字
     *   4. toUpperCase()：小文字 → 大文字（例：p-2026 → P-2026）
     *
     * @param policyNumber 入力された契約番号（全角・小文字が混在する可能性あり）
     * @return 正規化された契約番号（半角大文字）
     */
    public static String normalizePolicyNumber(String policyNumber) {
        // ① null または空白のみの場合は空文字を返す
        if (policyNumber == null || policyNumber.isBlank()) {
            return "";
        }

        // ② trim → NFKC正規化 → 大文字変換
        String normalized = Normalizer.normalize(policyNumber.trim(), Normalizer.Form.NFKC);
        return normalized.toUpperCase();
    }

    /**
     * 契約者名の正規化
     *
     * 契約者名は大文字変換しない（人名なので大文字化は不適切）。
     * NFKC正規化のみ行い、全角英数字の統一と結合文字の正規化を実施する。
     *
     * @param customerName 入力された契約者名
     * @return 正規化された契約者名
     */
    public static String normalizeCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return "";
        }

        // ③ 人名は NFKC + trim のみ（toUpperCase はしない）
        return Normalizer.normalize(customerName.trim(), Normalizer.Form.NFKC);
    }

    /**
     * 検索キーワードの正規化
     *
     * 契約番号検索・契約者名検索の両方に使われるため、
     * 大文字変換はせず NFKC + trim のみ行う。
     * （検索時は Repository の LIKE が大文字小文字を無視する設計で対応）
     *
     * @param keyword 入力された検索キーワード
     * @return 正規化された検索キーワード
     */
    public static String normalizeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }

        return Normalizer.normalize(keyword.trim(), Normalizer.Form.NFKC);
    }
}
