package jp.yoshiaki.insuranceapp.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * production では「本番用のEntity/Repository」だけを対象にするための設定。
 *
 * 目的:
 * - training(日次学習)で作った Entity (例: day66_notes など) を production 起動時に読み込まない
 * - Hibernate の validate で「存在しないテーブルがある」と起動が止まるのを防ぐ
 *
 * 前提:
 * - 本番用 Entity は jp.yoshiaki.insuranceapp.entity 配下
 * - 本番用 Repository は jp.yoshiaki.insuranceapp.repository 配下
 */
@Configuration
@Profile({"production", "stub"})
@EntityScan(basePackages = "jp.yoshiaki.insuranceapp.entity")
@EnableJpaRepositories(basePackages = "jp.yoshiaki.insuranceapp.repository")
public class ProductionJpaConfig {
}


