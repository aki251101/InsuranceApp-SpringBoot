package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("day90BookStoreHealthIndicator") // Bean名衝突回避（任意）
public class BookStoreHealthIndicator implements HealthIndicator {

    private final BookRepository repository;

    public BookStoreHealthIndicator(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        try {
            // 例：Repositoryが動くかの簡易チェック（必要に応じてあなたの実装に合わせて調整）
            repository.findAll();

            return Health.up()
                    .withDetail("bookStore", "ok")
                    .build();

        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("bookStore", "ng")
                    .build();
        }
    }
}