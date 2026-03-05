package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.AccidentDetailResponse;
import jp.yoshiaki.insuranceapp.dto.AccidentListResponse;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.service.AccidentService;
import jp.yoshiaki.insuranceapp.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 事故Controller
 */
@Controller
@RequestMapping("/accidents")
@RequiredArgsConstructor
@Slf4j
public class AccidentController {

    private final AccidentService accidentService;
    private final AiService aiService;

    @GetMapping
    public String list(
            @RequestParam(name = "tab", defaultValue = "OPEN_INPROGRESS") String tab,
            Model model) {

        log.debug("事故一覧表示: tab={}", tab);

        List<Accident> accidents = switch (tab) {
            case "RESOLVED" -> accidentService.getResolvedAccidents();
            default -> accidentService.getOpenAndInProgressAccidents();
        };

        AccidentListResponse response = AccidentListResponse.from(accidents);

        model.addAttribute("response", response);
        model.addAttribute("currentTab", tab);

        return "accident/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("事故詳細表示: id={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new IllegalArgumentException("事故が見つかりません: id=" + id));

        AccidentDetailResponse response = AccidentDetailResponse.from(accident);
        model.addAttribute("accident", response);

        return "accident/detail";
    }

    @PostMapping("/{id}/start-progress")
    public String startProgress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("事故対応開始: id={}", id);

        try {
            accidentService.startProgress(id);
            redirectAttributes.addFlashAttribute("successMessage", "対応を開始しました");
        } catch (Exception e) {
            log.error("事故対応開始エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("事故完了: id={}", id);

        try {
            accidentService.resolve(id);
            redirectAttributes.addFlashAttribute("successMessage", "事故を完了しました");
        } catch (Exception e) {
            log.error("事故完了エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/contacted")
    public String contacted(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("最終対応日更新: id={}", id);

        try {
            accidentService.updateLastContactedAt(id);
            redirectAttributes.addFlashAttribute("successMessage", "対応日時を更新しました");
        } catch (Exception e) {
            log.error("対応日時更新エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateMemo(
            @PathVariable Long id,
            @RequestParam String memo,
            RedirectAttributes redirectAttributes) {

        log.info("事故メモ更新: id={}", id);

        try {
            accidentService.updateMemo(id, memo);
            redirectAttributes.addFlashAttribute("successMessage", "メモを保存しました");
        } catch (Exception e) {
            log.error("メモ保存エラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました");
        }

        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/ai-suggest")
    public String aiSuggest(@PathVariable Long id, Model model) {
        log.info("AI次アクション候補生成: id={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new IllegalArgumentException("事故が見つかりません: id=" + id));

        try {
            String suggestion = aiService.suggestNextActions(accident);
            model.addAttribute("aiSuggestion", suggestion);
        } catch (Exception e) {
            log.error("AI API エラー", e);
            model.addAttribute("errorMessage", "AI応答の取得に失敗しました");
        }

        AccidentDetailResponse response = AccidentDetailResponse.from(accident);
        model.addAttribute("accident", response);

        return "accident/detail";
    }
}
