package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.dto.AccidentDetailResponse;
import jp.yoshiaki.insuranceapp.dto.AccidentListResponse;
import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.service.AccidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 事故Controller（REST API版）
 *
 * 完成版（insurance-app-java）は @Controller + Thymeleaf だが、
 * 統合初期段階では REST API として実装し、Postman で動作確認できるようにする。
 * 後日 Thymeleaf 画面を追加する際に @Controller 版へ切り替え可能。
 *
 * 【エンドポイント一覧】
 *   GET    /api/accidents              → 事故一覧（tab パラメータで絞り込み）
 *   GET    /api/accidents/{id}         → 事故詳細
 *   POST   /api/accidents              → 事故新規作成
 *   POST   /api/accidents/{id}/start-progress → 対応開始（OPEN → IN_PROGRESS）
 *   POST   /api/accidents/{id}/resolve        → 完了（IN_PROGRESS → RESOLVED）
 *   POST   /api/accidents/{id}/contacted      → 最終対応日を更新
 *   PUT    /api/accidents/{id}/memo           → メモを更新
 */
@RestController
@RequestMapping("/api/accidents")
@RequiredArgsConstructor
@Slf4j
public class AccidentController {

    private final AccidentService accidentService;

    /**
     * 事故一覧を取得する
     *
     * tab パラメータで表示内容を切り替える:
     *   - "RESOLVED"        → 完了した事故のみ
     *   - それ以外（デフォルト）→ 対応中の事故（OPEN + IN_PROGRESS）
     *
     * @param tab タブ指定（"OPEN_INPROGRESS" or "RESOLVED"）
     * @return 事故一覧DTO
     */
    @GetMapping
    public ResponseEntity<AccidentListResponse> list(
            @RequestParam(name = "tab", defaultValue = "OPEN_INPROGRESS") String tab) {

        log.debug("事故一覧取得: tab={}", tab);

        List<Accident> accidents = switch (tab) {
            case "RESOLVED" -> accidentService.getResolvedAccidents();
            default -> accidentService.getOpenAndInProgressAccidents();
        };

        AccidentListResponse response = AccidentListResponse.from(accidents);
        return ResponseEntity.ok(response);
    }

    /**
     * 事故詳細を取得する
     *
     * @param id 事故ID（パスパラメータ）
     * @return 事故詳細DTO（操作可否フラグ含む）
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccidentDetailResponse> detail(@PathVariable Long id) {
        log.debug("事故詳細取得: id={}", id);

        Accident accident = accidentService.getAccidentById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "事故が見つかりません: id=" + id));

        AccidentDetailResponse response = AccidentDetailResponse.from(accident);
        return ResponseEntity.ok(response);
    }

    /**
     * 事故を新規作成する
     *
     * リクエストボディ例:
     * {
     *   "policyId": 1,
     *   "occurredAt": "2026-03-01",
     *   "place": "熊本市中央区",
     *   "description": "追突事故"
     * }
     *
     * @param accident 事故データ（JSON → Entity にバインド）
     * @return 作成された事故の詳細DTO
     */
    @PostMapping
    public ResponseEntity<AccidentDetailResponse> create(
            @RequestBody Accident accident) {

        log.info("事故作成: policyId={}", accident.getPolicyId());

        Accident saved = accidentService.createAccident(accident);
        AccidentDetailResponse response = AccidentDetailResponse.from(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 対応開始する（OPEN → IN_PROGRESS）
     *
     * @param id 事故ID
     * @return 更新された事故の詳細DTO
     */
    @PostMapping("/{id}/start-progress")
    public ResponseEntity<?> startProgress(@PathVariable Long id) {
        log.info("事故対応開始: id={}", id);

        try {
            Accident updated = accidentService.startProgress(id);
            AccidentDetailResponse response = AccidentDetailResponse.from(updated);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 不正な遷移の場合
            log.warn("対応開始失敗: id={}, reason={}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 事故が見つからない場合
            log.warn("事故が見つかりません: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 完了する（IN_PROGRESS → RESOLVED）
     *
     * @param id 事故ID
     * @return 更新された事故の詳細DTO
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long id) {
        log.info("事故完了: id={}", id);

        try {
            Accident updated = accidentService.resolve(id);
            AccidentDetailResponse response = AccidentDetailResponse.from(updated);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("完了失敗: id={}, reason={}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("事故が見つかりません: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 最終対応日を更新する（「対応した」ボタン）
     *
     * @param id 事故ID
     * @return 更新された事故の詳細DTO
     */
    @PostMapping("/{id}/contacted")
    public ResponseEntity<?> contacted(@PathVariable Long id) {
        log.info("最終対応日更新: id={}", id);

        try {
            Accident updated = accidentService.updateLastContactedAt(id);
            AccidentDetailResponse response = AccidentDetailResponse.from(updated);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("対応日更新失敗: id={}, reason={}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("事故が見つかりません: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * メモを更新する
     *
     * リクエストボディ例:
     * {
     *   "memo": "お客様に連絡済み。修理工場の手配待ち。"
     * }
     *
     * @param id   事故ID
     * @param body メモを含むJSON
     * @return 更新された事故の詳細DTO
     */
    @PutMapping("/{id}/memo")
    public ResponseEntity<?> updateMemo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("事故メモ更新: id={}", id);

        String memo = body.getOrDefault("memo", "");

        try {
            Accident updated = accidentService.updateMemo(id, memo);
            AccidentDetailResponse response = AccidentDetailResponse.from(updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("事故が見つかりません: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
