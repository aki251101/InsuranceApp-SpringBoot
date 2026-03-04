package jp.yoshiaki.insuranceapp.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.yoshiaki.insuranceapp.domain.accident.Accident;
import jp.yoshiaki.insuranceapp.service.AccidentService;

/**
 * Day89: 事故情報 → AccidentService.suggestNextActions の動線確認用 Controller。
 *
 * Controller は「入口」として、Service に処理を委譲する。
 * ログ（AI次アクション候補生成: id=...）は Service 側で出す想定。
 */
@RestController
@RequestMapping("/accidents")
public class AccidentController {

    private final AccidentService accidentService;

    public AccidentController(AccidentService accidentService) {
        this.accidentService = accidentService;
    }

    @GetMapping("/{id}")
    public Accident get(@PathVariable Long id) {
        return accidentService.findById(id);
    }

    @GetMapping
    public List<Accident> list() {
        return accidentService.list();
    }

    /**
     * Day89: AI次アクション候補（Stub なら固定テンプレ文字列）
     */
    @GetMapping(value = "/{id}/next-actions", produces = MediaType.TEXT_PLAIN_VALUE)
    public String suggestNextActions(@PathVariable Long id) {
        return accidentService.suggestNextActions(id);
    }
}
