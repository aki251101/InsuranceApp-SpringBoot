package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.AccidentCreateRequest;
import jp.yoshiaki.insuranceapp.dto.AccidentDetailResponse;
import jp.yoshiaki.insuranceapp.dto.AccidentListResponse;
import jp.yoshiaki.insuranceapp.dto.AccidentMemoResponse;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.service.AccidentService;
import jp.yoshiaki.insuranceapp.service.AiService;
import jp.yoshiaki.insuranceapp.service.AiUsageLimitService;
import jp.yoshiaki.insuranceapp.service.ListSortService;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

    private static final DateTimeFormatter DATE_TIME_INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final AccidentService accidentService;
    private final AiService aiService;
    private final AiUsageLimitService aiUsageLimitService;
    private final ListSortService listSortService;
    private final PolicyService policyService;

    @GetMapping
    public String list(
            @RequestParam(name = "tab", defaultValue = "OPEN_INPROGRESS") String tab,
            @RequestParam(name = "sort", defaultValue = "occurredAt") String sort,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {

        log.debug("事故一覧画面表示: tab={}, sort={}, direction={}", tab, sort, direction);

        String currentTab = switch (tab) {
            case "OPEN_INPROGRESS", "RESOLVED", "ALL" -> tab;
            default -> "OPEN_INPROGRESS";
        };

        List<Accident> accidents = switch (currentTab) {
            case "RESOLVED" -> accidentService.getResolvedAccidents();
            case "ALL" -> accidentService.getAllAccidents();
            default -> accidentService.getOpenAndInProgressAccidents();
        };

        String currentSort = listSortService.normalizeAccidentSort(sort);
        String currentDirection = listSortService.normalizeDirection(direction);
        accidents = listSortService.sortAccidents(accidents, currentSort, currentDirection);

        model.addAttribute("response", AccidentListResponse.from(accidents));
        model.addAttribute("currentTab", currentTab);
        model.addAttribute("currentSort", currentSort);
        model.addAttribute("currentDirection", currentDirection);
        return "accident/list";
    }

    @GetMapping("/new")
    public String newForm(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "policyId", required = false) Long policyId,
            Model model) {
        if (!model.containsAttribute("accidentCreateRequest")) {
            AccidentCreateRequest request = new AccidentCreateRequest();
            request.setPolicyId(policyId);
            model.addAttribute("accidentCreateRequest", request);
        }
        populateNewFormModel(model, q, policyId);
        return "accident/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("accidentCreateRequest") AccidentCreateRequest request,
            BindingResult bindingResult,
            @RequestParam(name = "q", required = false) String q,
            Model model,
            RedirectAttributes redirectAttributes) {
        log.info("事故新規登録: policyId={}", request.getPolicyId());

        Policy selectedPolicy = null;
        if (request.getPolicyId() != null) {
            try {
                selectedPolicy =
                        policyService.getActivePolicyForAccidentRegistration(request.getPolicyId());
            } catch (RuntimeException e) {
                bindingResult.rejectValue("policyId", "invalidPolicy", e.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "入力漏れまたは入力内容の誤りがあります。");
            populateNewFormModel(model, q, request.getPolicyId(), selectedPolicy);
            return "accident/new";
        }

        Accident accident = Accident.builder()
                .policyId(request.getPolicyId())
                .occurredAt(request.getOccurredAt())
                .place(request.getPlace())
                .description(request.getDescription())
                .build();
        Accident saved = accidentService.createAccident(accident);
        redirectAttributes.addFlashAttribute("successMessage", "事故を登録しました");

        return "redirect:/accidents/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("事故詳細画面表示: id={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new NotFoundException("事故が見つかりません: id=" + id));

        model.addAttribute("accident", AccidentDetailResponse.from(accident));
        model.addAttribute("memos", accidentService.getMemos(id).stream()
                .sorted(Comparator.comparing(memo -> memo.getHandledAt()))
                .map(AccidentMemoResponse::from)
                .toList());
        model.addAttribute("defaultHandledAt",
                LocalDateTime.now().format(DATE_TIME_INPUT_FORMATTER));
        return "accident/detail";
    }

    @PostMapping("/{id}/start-progress")
    public String startProgress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accidentService.startProgress(id);
        redirectAttributes.addFlashAttribute("successMessage", "事故対応を開始しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accidentService.resolve(id);
        redirectAttributes.addFlashAttribute("successMessage", "事故対応を完了しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/contacted")
    public String contacted(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accidentService.updateLastContactedAt(id);
        redirectAttributes.addFlashAttribute("successMessage", "最終対応日時を更新しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateMemo(
            @PathVariable Long id,
            @RequestParam(name = "memo", defaultValue = "") String memo,
            RedirectAttributes redirectAttributes) {
        accidentService.updateMemo(id, memo);
        redirectAttributes.addFlashAttribute("successMessage", "対応履歴メモを保存しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/memos")
    public String addMemo(
            @PathVariable Long id,
            @RequestParam("handledAt") String handledAt,
            @RequestParam("content") String content,
            @RequestParam(name = "updateLastContactedAt", defaultValue = "false")
            boolean updateLastContactedAt,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        accidentService.addMemo(
                id,
                parseHandledAt(handledAt),
                content,
                authentication != null ? authentication.getName() : null,
                updateLastContactedAt);
        redirectAttributes.addFlashAttribute("successMessage", "対応履歴を追加しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/memos/{memoId}")
    public String updateMemoEntry(
            @PathVariable Long id,
            @PathVariable Long memoId,
            @RequestParam("handledAt") String handledAt,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {
        accidentService.updateMemoEntry(id, memoId, parseHandledAt(handledAt), content);
        redirectAttributes.addFlashAttribute("successMessage", "対応履歴を更新しました");
        return "redirect:/accidents/" + id;
    }

    @PostMapping("/{id}/ai-suggest")
    public String aiSuggest(@PathVariable Long id, Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        log.info("AI次アクション候補生成: accidentId={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new NotFoundException("事故が見つかりません: id=" + id));
        if ("RESOLVED".equals(accident.getStatus())) {
            throw new IllegalStateException("対応が完了した事故ではAI提案を利用できません。");
        }

        aiUsageLimitService.consume(authentication != null ? authentication.getName() : null);
        accident.setMemo(accidentService.getMemoText(id));

        String suggestion = aiService.suggestNextActions(accident);
        redirectAttributes.addFlashAttribute("aiSuggestion", suggestion);
        redirectAttributes.addFlashAttribute("successMessage", "AI提案を生成しました");

        return "redirect:/accidents/" + id;
    }

    private LocalDateTime parseHandledAt(String handledAt) {
        try {
            return LocalDateTime.parse(handledAt, DATE_TIME_INPUT_FORMATTER);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("対応日時を正しく入力してください");
        }
    }

    private void populateNewFormModel(Model model, String q, Long policyId) {
        populateNewFormModel(model, q, policyId, null);
    }

    private void populateNewFormModel(Model model, String q, Long policyId, Policy selectedPolicy) {
        String query = q != null ? q : "";
        List<Policy> searchResults = policyService.searchActivePolicies(query);
        Policy policy = selectedPolicy;
        if (policy == null && policyId != null) {
            policy = policyService.getActivePolicyForAccidentRegistration(policyId);
        }

        model.addAttribute("policyQuery", query);
        model.addAttribute("policySearchResults", searchResults);
        model.addAttribute("selectedPolicy", policy);
        model.addAttribute("hasPolicySearch", q != null && !q.isBlank());
    }
}
