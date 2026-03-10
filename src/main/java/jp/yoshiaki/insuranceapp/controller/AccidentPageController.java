package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.AccidentDetailResponse;
import jp.yoshiaki.insuranceapp.dto.AccidentListResponse;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.service.AccidentService;
import jp.yoshiaki.insuranceapp.service.AiService;
import jp.yoshiaki.insuranceapp.service.AiUsageLimitService;
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
 * 事故画面Controller（Thymeleaf）
 *
 * REST API（/api/accidents）とは別に、画面遷移用のエンドポイントを提供する。
 */
@Controller
@RequestMapping("/accidents")
@RequiredArgsConstructor
@Slf4j
public class AccidentPageController {

    private final AccidentService accidentService;
    private final AiService aiService;
    private final AiUsageLimitService aiUsageLimitService;

    @GetMapping
    public String list(
            @RequestParam(name = "tab", defaultValue = "OPEN_INPROGRESS") String tab,
            Model model) {

        log.debug("事故一覧画面表示: tab={}", tab);

        List<Accident> accidents = switch (tab) {
            case "RESOLVED" -> accidentService.getResolvedAccidents();
            default -> accidentService.getOpenAndInProgressAccidents();
        };

        model.addAttribute("response", AccidentListResponse.from(accidents));
        model.addAttribute("currentTab", tab);
        return "accident/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("事故詳細画面表示: id={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new NotFoundException("事故が見つかりません: id=" + id));

        model.addAttribute("accident", AccidentDetailResponse.from(accident));
        return "accident/detail";
    }

    @PostMapping("/{id}/ai-suggest")
    public String aiSuggest(@PathVariable Long id, Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        log.info("AI次アクション候補生成: accidentId={}", id);

        aiUsageLimitService.consume(authentication != null ? authentication.getName() : null);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new NotFoundException("事故が見つかりません: id=" + id));

        String suggestion = aiService.suggestNextActions(accident);
        redirectAttributes.addFlashAttribute("aiSuggestion", suggestion);
        redirectAttributes.addFlashAttribute("successMessage", "AI提案を生成しました");

        return "redirect:/accidents/" + id;
    }
}
