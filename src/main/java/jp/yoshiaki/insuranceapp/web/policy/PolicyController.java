package jp.yoshiaki.insuranceapp.web.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.dto.policy.PolicyDetailResponse;
import jp.yoshiaki.insuranceapp.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 契約Controller
 *
 * 【Day92で追加】
 * - POST /policies/{id}/renew     : 更新
 * - POST /policies/{id}/unrenew   : 更新取消
 * - POST /policies/{id}/cancel    : 解約
 * - POST /policies/{id}/uncancel  : 解約取消
 */
@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    // ─── Day91で作成済みのエンドポイント（既存） ───

    /**
     * 契約一覧画面
     */
    @GetMapping
    public String list(Model model) {
        log.debug("契約一覧表示");
        List<Policy> policies = policyService.getAllPolicies();
        model.addAttribute("policies", policies);
        return "policy/list";
    }

    /**
     * 契約詳細画面
     *
     * 【Day92変更点】PolicyDetailResponse.from() で操作可否フラグを渡す
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("契約詳細表示: id={}", id);

        Policy policy = policyService.getPolicyById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません: id=" + id));

        // DTO変換（操作可否フラグを含む）
        PolicyDetailResponse response = PolicyDetailResponse.from(policy);
        model.addAttribute("policy", response);

        return "policy/detail";
    }

    // ─── Day92で追加するエンドポイント ───

    /**
     * 契約更新（更新ボタン押下時）
     *
     * 成功/失敗をフラッシュメッセージで詳細画面にリダイレクト。
     * フラッシュメッセージ（flash attribute）とは：
     *   リダイレクト先の画面に1回だけ表示されるメッセージ。
     *   画面をリロードすると消える。「操作結果の通知」に使う。
     */
    @PostMapping("/{id}/renew")
    public String renew(@PathVariable Long id,
                        RedirectAttributes redirectAttributes) {
        log.info("契約更新: id={}", id);

        try {
            policyService.renewPolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "契約を更新しました");
        } catch (IllegalStateException e) {
            // 業務ルール違反（更新可能期間外など）
            log.warn("契約更新失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // 予期しないエラー
            log.error("契約更新エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }

    /**
     * 契約更新取消（当日限定）
     */
    @PostMapping("/{id}/unrenew")
    public String unrenew(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        log.info("契約更新取消: id={}", id);

        try {
            policyService.unrenewPolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "更新を取り消しました");
        } catch (IllegalStateException e) {
            log.warn("契約更新取消失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("契約更新取消エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }

    /**
     * 契約解約
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        log.info("契約解約: id={}", id);

        try {
            policyService.cancelPolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "契約を解約しました");
        } catch (IllegalStateException e) {
            log.warn("契約解約失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("契約解約エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }

    /**
     * 契約解約取消（当日限定）
     */
    @PostMapping("/{id}/uncancel")
    public String uncancel(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        log.info("契約解約取消: id={}", id);

        try {
            policyService.uncancelPolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "解約を取り消しました");
        } catch (IllegalStateException e) {
            log.warn("契約解約取消失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("契約解約取消エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }
}
