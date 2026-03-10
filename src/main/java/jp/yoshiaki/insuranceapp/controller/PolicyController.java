package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.PolicyDetailResponse;
import jp.yoshiaki.insuranceapp.dto.PolicyListResponse;
import jp.yoshiaki.insuranceapp.dto.RenewalStatsDto;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.service.AiService;
import jp.yoshiaki.insuranceapp.service.AiUsageLimitService;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import jp.yoshiaki.insuranceapp.service.RenewalStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final AiService aiService;
    private final AiUsageLimitService aiUsageLimitService;

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
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        PolicyDetailResponse response = PolicyDetailResponse.from(policy);
        model.addAttribute("policy", response);

        return "policy/detail";
    }

    /**
     * Day95: 契約のAI要約（保存しない）
     */
    @PostMapping("/{id}/ai-summarize")
    public String aiSummarize(@PathVariable Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        log.info("契約AI要約: id={}", id);

        aiUsageLimitService.consume(authentication != null ? authentication.getName() : null);

        Policy policy = policyService.getPolicyById(id)
                .orElseThrow(() -> new NotFoundException("契約が見つかりません: id=" + id));

        String summary = aiService.summarizePolicy(policy);
        redirectAttributes.addFlashAttribute("aiSummary", summary);
        redirectAttributes.addFlashAttribute("successMessage", "AI要約を生成しました");

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/renew")
    public String renew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("契約更新: id={}", id);

        policyService.renewPolicy(id);
        redirectAttributes.addFlashAttribute("successMessage", "契約を更新しました");

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/unrenew")
    public String unrenew(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("契約更新取消: id={}", id);

        policyService.unrenewPolicy(id);
        redirectAttributes.addFlashAttribute("successMessage", "更新を取り消しました");

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("契約解約: id={}", id);

        policyService.cancelPolicy(id);
        redirectAttributes.addFlashAttribute("successMessage", "契約を解約しました");

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/uncancel")
    public String uncancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("契約解約取消: id={}", id);

        policyService.uncancelPolicy(id);
        redirectAttributes.addFlashAttribute("successMessage", "解約を取り消しました");

        return "redirect:/policies/" + id;
    }

    @PostMapping("/{id}/calendar-toggle")
    public String toggleCalendar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("カレンダー登録トグル: id={}", id);

        Policy policy = policyService.toggleCalendarRegistration(id);

        if (policy.getCalendarRegistered()) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "カレンダーに登録しました（満期7日前に通知されます）");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "カレンダー登録を解除しました");
        }

        return "redirect:/policies/" + id;
    }
}
