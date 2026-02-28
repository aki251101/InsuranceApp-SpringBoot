package jp.yoshiaki.insuranceapp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.domain.Accident;
import jp.yoshiaki.insuranceapp.domain.exception.NotFoundException;
import jp.yoshiaki.insuranceapp.domain.exception.ValidationException;
import jp.yoshiaki.insuranceapp.repository.AccidentRepository;

@Service
public class AccidentService {

    private static final Logger log = LoggerFactory.getLogger(AccidentService.class);

    private final AccidentRepository accidentRepository;
    private final AiClient aiClient;

    public AccidentService(AccidentRepository accidentRepository, AiClient aiClient) {
        this.accidentRepository = accidentRepository;
        this.aiClient = aiClient;
    }

    public Accident findById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive: id=" + id);
        }

        Accident accident = accidentRepository.findById(id).orElse(null);
        if (accident == null) {
            throw new NotFoundException("accident not found: id=" + id);
        }
        return accident;
    }

    public List<Accident> list() {
        return accidentRepository.findAll();
    }

    /**
     * Day89: AI次アクション候補を生成する（Stubなら固定文が返る）
     */
    public String suggestNextActions(Long id) {
        // 学習ログ（動線確認）
        log.info("AI次アクション候補生成: id={}", id);

        Accident accident = findById(id);
        return aiClient.suggestNextActions(accident);
    }
}
