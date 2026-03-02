// 配置：src/main/java/jp/yoshiaki/insuranceapp/dto/policy/PolicyCreateRequest.java
package jp.yoshiaki.insuranceapp.dto.policy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 契約作成リクエスト DTO。
 *
 * クライアント（Postman や画面）から送られてくる JSON を受け取る箱。
 * Entity（Policy）をそのまま受け取らず、DTO を挟む理由は：
 *   ① 「受け取りたいフィールドだけ」に絞れる（id や created_at は受け取らない）
 *   ② Entity の構造変更が API の入力形式に直接影響しない（疎結合）
 *
 * Day91 のスコープでは customerName と startDate の2つだけ受け取る。
 * policyNumber は自動附番、endDate は startDate + 1年で自動計算する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCreateRequest {

    /** 契約者名（必須） */
    private String customerName;

    /** 契約開始日（必須。例："2026-04-01"） */
    private LocalDate startDate;
}
