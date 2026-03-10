package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ヘルスチェックService
 *
 * 各コンポーネント（DB/外部API）の「生きているか？」を確認し、
 * 結果をHealthResultにまとめて返す。
 *
 * 実務では Spring Boot Actuator の /actuator/health がこの役割を果たすが、
 * 今回は仕組みを理解するため手作りする。
 *
 * 考え方：
 * - DB疎通 → Repository.count() が例外なく返ればOK
 * - 外部API疎通 → ExternalServiceStub.ping() がtrueを返せばOK
 * - 1つでもDOWNがあれば全体ステータスもDOWN
 */
@Service("day99HealthCheckService")
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final ProductRepository productRepository;
    private final ExternalServiceStub externalServiceStub;

    public HealthCheckService(ProductRepository productRepository,
                              ExternalServiceStub externalServiceStub) {
        this.productRepository = productRepository;
        this.externalServiceStub = externalServiceStub;
    }

    /**
     * 全コンポーネントのヘルスチェックを実行する
     *
     * @return ヘルスチェック結果（各コンポーネントのUP/DOWNと全体ステータス）
     */
    public HealthResult checkAll() {
        HealthResult result = new HealthResult();

        // ① DB疎通チェック
        result.addComponent("db", checkDb());

        // ② 外部API疎通チェック
        result.addComponent("externalApi", checkExternalApi());

        // ③ 全体ステータスを計算（1つでもDOWN → DOWN）
        result.calculateOverallStatus();

        log.info("ヘルスチェック完了: status={}, components={}",
                result.getStatus(), result.getComponents());

        return result;
    }

    /**
     * DB疎通チェック
     * count()が例外なく返ればUP、例外が出ればDOWN
     */
    private boolean checkDb() {
        try {
            long count = productRepository.count();
            log.debug("DB疎通OK: 商品件数={}", count);
            return true;
        } catch (Exception e) {
            log.error("DB疎通失敗: {}", e.getMessage()); // ④ 原因をログに残す
            return false;
        }
    }

    /**
     * 外部API疎通チェック
     * ping()がtrueを返せばUP、例外が出ればDOWN
     */
    private boolean checkExternalApi() {
        try {
            boolean ok = externalServiceStub.ping();
            log.debug("外部API疎通OK");
            return ok;
        } catch (Exception e) {
            log.error("外部API疎通失敗: {}", e.getMessage());
            return false;
        }
    }
}
