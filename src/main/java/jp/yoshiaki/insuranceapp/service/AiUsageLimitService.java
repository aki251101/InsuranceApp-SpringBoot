package jp.yoshiaki.insuranceapp.service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI機能の1日利用回数をユーザー単位で制御する簡易リミッター。
 */
@Service
@Slf4j
public class AiUsageLimitService {

    private final int dailyLimit;
    private final ConcurrentHashMap<String, UsageCounter> counters = new ConcurrentHashMap<>();

    public AiUsageLimitService(@Value("${ai.usage-limit.daily-per-user:20}") int dailyLimit) {
        if (dailyLimit <= 0) {
            throw new IllegalArgumentException("ai.usage-limit.daily-per-user は1以上を指定してください");
        }
        this.dailyLimit = dailyLimit;
    }

    public void consume(String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalStateException("AI機能の利用にはログインが必要です。");
        }

        LocalDate today = LocalDate.now();
        AtomicBoolean denied = new AtomicBoolean(false);

        counters.compute(userKey, (k, existing) -> {
            if (existing == null || !existing.date.equals(today)) {
                return new UsageCounter(today, 1);
            }
            if (existing.count >= dailyLimit) {
                denied.set(true);
                return existing;
            }
            return new UsageCounter(today, existing.count + 1);
        });

        if (denied.get()) {
            log.warn("AI利用上限に到達: user={}, dailyLimit={}", userKey, dailyLimit);
            throw new IllegalStateException("AI機能の1日利用上限（" + dailyLimit + "回）に達しました。明日以降に再試行してください。");
        }
    }

    private record UsageCounter(LocalDate date, int count) {
    }
}
