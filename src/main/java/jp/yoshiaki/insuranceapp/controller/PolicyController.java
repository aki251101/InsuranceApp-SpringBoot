package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.PolicyDetailResponse;
import jp.yoshiaki.insuranceapp.dto.PolicyListResponse;
import jp.yoshiaki.insuranceapp.dto.RenewalStatsDto;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import jp.yoshiaki.insuranceapp.service.RenewalStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 契約Controller
 */
@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyService policyService;
    private final RenewalStatsService renewalStatsService;

    @GetMapping
    public String list(
            @RequestParam(name = "tab", defaultValue = "RENEWABLE") String tab,
            @RequestParam(name = "q", required = false) String q,
            Model model) {

        log.debug("契約一覧表示: tab={}, q={}", tab, q);

        List<Policy> policies;

        if (q != null && !q.isBlank()) {
            policies = policyService.searchPolicies(q);
        } else {
            policies = switch (tab) {
                case "RENEWABLE" -> policyService.getRenewablePolicies();
                case "ACTIVE" -> policyService.getPoliciesByStatus("ACTIVE");
                case "CANCELLED" -> policyService.getPoliciesByStatus("CANCELLED");
                case "LAPSED" -> policyService.getLapsedPolicies();
                default -> policyService.getAllPolicies();
            };
        }

        RenewalStatsDto stats = renewalStatsService.getStats();
        PolicyListResponse response = PolicyListResponse.from(policies, stats);

        model.addAttribute("response", response);
        model.addAttribute("currentTab", tab);
        model.addAttribute("searchQuery", q != null ? q : "");

        return "policy/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("契約詳細表示: id={}", id);

        Policy policy = policyService.getPolicyById(id)
                .orElseThrow(() -> new IllegalArgumentException("契約が見つかりません: id=" + id));

        PolicyDetailResponse response = PolicyDetailResponse.from(policy);
        model.addAttribute("policy", response);

        return "policy/detail";
    }

    @PostMapping("/{id}/renew")
    public String renew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("契約更新: id={}", id);

        try {
            policyService.renewPolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "契約を更新しました");
        } catch (IllegalStateException e) {
            log.warn("契約更新失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("契約更新エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/unrenew")
    public String unrenew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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

    @PostMapping("/{id}/uncancel")
    public String uncancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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

    @PostMapping("/{id}/calendar-toggle")
    public String toggleCalendar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("カレンダー登録トグル: id={}", id);

        try {
            Policy policy = policyService.toggleCalendarRegistration(id);

            if (policy.getCalendarRegistered()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "カレンダーに登録しました（満期7日前に通知されます）");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "カレンダー登録を解除しました");
            }
        } catch (Exception e) {
            log.error("カレンダー登録エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "カレンダー連携でエラーが発生しました");
        }

        return "redirect:/policies/" + id;
    }
}
