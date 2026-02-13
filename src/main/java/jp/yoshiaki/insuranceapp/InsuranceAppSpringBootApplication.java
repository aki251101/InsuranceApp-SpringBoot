package jp.yoshiaki.insuranceapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InsuranceAppSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceAppSpringBootApplication.class, args);
    }
}
