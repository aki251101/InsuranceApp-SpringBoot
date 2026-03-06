package jp.yoshiaki.insuranceapp.config;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.client.GeminiAiClient;
import jp.yoshiaki.insuranceapp.client.stub.StubAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AIクライアントの切替設定
 *
 * application.yml の ai.client.type の値に基づき、
 * StubAiClient または GeminiAiClient をBeanとして登録する。
 *
 * 設定例（application.yml）：
 *   ai:
 *     client:
 *       type: stub     ← 開発環境（API Key不要）
 *       type: gemini   ← 本番環境（API Key必要）
 */
@Configuration
@Slf4j
public class AiClientConfig {

    /**
     * Stub用AIクライアントをBeanとして登録
     *
     * ai.client.type=stub のとき、または ai.client.type が
     * 設定されていないとき（matchIfMissing=true）に有効。
     *
     * @return StubAiClient
     */
    @Bean
    @ConditionalOnProperty(
            name = "ai.client.type",
            havingValue = "stub",
            matchIfMissing = true  // ← 設定がない場合もStubがデフォルト（安全側に倒す）
    )
    public AiClient stubAiClient() {
        log.info("AIクライアント: Stub（固定応答）モードで起動");
        return new StubAiClient();
    }

    /**
     * Gemini API用AIクライアントをBeanとして登録
     *
     * ai.client.type=gemini のときのみ有効。
     *
     * @return GeminiAiClient
     */
    @Bean
    @ConditionalOnProperty(
            name = "ai.client.type",
            havingValue = "gemini"
    )
    public AiClient geminiAiClient() {
        log.info("AIクライアント: Gemini API（本番）モードで起動");
        return new GeminiAiClient();
    }
}
