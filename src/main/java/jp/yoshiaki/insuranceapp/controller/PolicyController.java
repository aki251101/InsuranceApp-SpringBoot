package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/policies")
    public String list(@RequestParam(required = false) String tab,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        String currentTab = (tab == null || tab.isBlank()) ? "all" : tab;
        String currentKeyword = (keyword == null) ? "" : keyword.trim();

        List<Policy> policies = policyService.listAll();

        // キーワード検索（証券番号/氏名/車両情報/契約内容）
        if (!currentKeyword.isBlank()) {
            policies = policies.stream()
                .filter(p ->
                    contains(p.getPolicyNumber(), currentKeyword)
                    || contains(p.getCustomerName(), currentKeyword)
                    || contains(p.getVehicleInfo(), currentKeyword)
                    || contains(p.getContractContent(), currentKeyword)
                )
                .toList();
        }

        // タブ絞り込み
        policies = switch (currentTab) {
            case "renewable" -> policies.stream()
                .filter(Policy::isRenewable)
                .toList();
            case "lapsed" -> policies.stream()
                .filter(p -> p.getEffectiveStatus() == Policy.EffectiveStatus.LAPSED)
                .toList();
            default -> policies;
        };

        model.addAttribute("policies", policies);
        model.addAttribute("tab", currentTab);
        model.addAttribute("keyword", currentKeyword);
        return "policy/list";
    }

    @PostMapping("/policies/{id}/calendar-toggle")
    public String toggleCalendar(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        log.info("カレンダー登録トグル: id={}", id);
        try {
            policyService.toggleCalendar(id);
            redirectAttributes.addFlashAttribute("successMessage", "カレンダー登録状態を更新しました");
        } catch (Exception e) {
            log.error("カレンダー登録トグル失敗", e);
            redirectAttributes.addFlashAttribute("errorMessage", "カレンダー登録の更新に失敗しました");
        }
        return "redirect:/policies";
    }

    @PostMapping("/policies/{id}/renew")
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

    private boolean contains(String value, String keyword) {
        if (value == null || keyword == null) return false;
        return value.toLowerCase().contains(keyword.toLowerCase());
    }
}
