// 配置：src/main/java/jp/yoshiaki/insuranceapp/web/policy/PolicyController.java
package jp.yoshiaki.insuranceapp.web.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.dto.policy.PolicyCreateRequest;
import jp.yoshiaki.insuranceapp.dto.policy.PolicyResponse;
import jp.yoshiaki.insuranceapp.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 契約 REST Controller。
 *
 * エンドポイント一覧（Day91 スコープ）：
 *   POST   /api/policies       → 契約を新規作成
 *   GET    /api/policies       → 契約一覧（検索キーワード任意）
 *   GET    /api/policies/{id}  → 契約詳細（1件取得）
 *
 * Day92 で追加予定：
 *   POST   /api/policies/{id}/renew     → 更新
 *   POST   /api/policies/{id}/cancel    → 解約
 *   POST   /api/policies/{id}/unrenew   → 更新取消
 *   POST   /api/policies/{id}/uncancel  → 解約取消
 */
@RestController                            // ① JSON を返す Controller であることを宣言
@RequestMapping("/api/policies")           // ② このクラスの全メソッドのURLは /api/policies で始まる
@RequiredArgsConstructor                   // ③ final フィールドを引数に持つコンストラクタを自動生成（DI用）
@Slf4j                                     // ④ ログ出力用の log 変数を自動生成
public class PolicyController {

    /** 契約の業務ロジックを持つ Service */
    private final PolicyService policyService;

    // ── 作成 ──────────────────────────────

    /**
     * 契約を新規作成する。
     *
     * リクエスト例（JSON）：
     *   {
     *     "customerName": "山田太郎",
     *     "startDate": "2026-04-01"
     *   }
     *
     * @param request 作成リクエスト DTO
     * @return 作成された契約（201 Created）
     */
    @PostMapping
    public ResponseEntity<PolicyResponse> create(
            @RequestBody PolicyCreateRequest request) {

        log.info("契約作成リクエスト: customerName={}, startDate={}",
                request.getCustomerName(), request.getStartDate());

        // DTO → Entity に変換（必要なフィールドだけ詰める）
        Policy policy = Policy.builder()
                .customerName(request.getCustomerName())
                .startDate(request.getStartDate())
                .build();

        // Service に作成を委譲
        Policy created = policyService.createPolicy(policy);

        // Entity → DTO に変換してレスポンスを返す
        PolicyResponse response = PolicyResponse.from(created);

        // HTTP 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── 一覧・検索 ──────────────────────────────

    /**
     * 契約一覧を取得する。
     * クエリパラメータ q が指定されていれば部分一致検索、
     * 指定されていなければ全件取得する。
     *
     * リクエスト例：
     *   GET /api/policies         → 全件取得
     *   GET /api/policies?q=山田  → 「山田」で部分一致検索
     *
     * @param q 検索キーワード（任意）
     * @return 契約リスト（200 OK）
     */
    @GetMapping
    public ResponseEntity<List<PolicyResponse>> list(
            @RequestParam(name = "q", required = false) String q) {

        log.debug("契約一覧リクエスト: q={}", q);

        // 検索キーワードの有無で処理を分岐
        List<Policy> policies;
        if (q != null && !q.isBlank()) {
            policies = policyService.searchPolicies(q);
        } else {
            policies = policyService.getAllPolicies();
        }

        // Entity リスト → DTO リスト に変換
        List<PolicyResponse> responseList = policies.stream()
                .map(PolicyResponse::from)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    // ── 詳細 ──────────────────────────────

    /**
     * 契約詳細を1件取得する。
     * 見つからない場合は NotFoundException → GlobalExceptionHandler → 404。
     *
     * リクエスト例：
     *   GET /api/policies/1
     *
     * @param id 契約ID（パスパラメータ）
     * @return 契約詳細（200 OK）
     */
    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> detail(@PathVariable Long id) {

        log.debug("契約詳細リクエスト: id={}", id);

        // Service から取得（見つからなければ NotFoundException）
        Policy policy = policyService.getPolicyById(id);

        // Entity → DTO に変換
        PolicyResponse response = PolicyResponse.from(policy);

        return ResponseEntity.ok(response);
    }
}
