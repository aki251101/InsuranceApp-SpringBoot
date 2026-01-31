package jp.yoshiaki.insuranceapp;

import jp.yoshiaki.insuranceapp.service.GreetingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InsuranceAppSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceAppSpringBootApplication.class, args);
    }

    @Bean
    CommandLineRunner run(GreetingService greetingService) {
        return args -> System.out.println(greetingService.greet("Ken"));
    }
}
