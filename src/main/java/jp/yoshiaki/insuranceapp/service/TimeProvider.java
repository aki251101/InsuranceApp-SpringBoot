package jp.yoshiaki.insuranceapp.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeProvider {
    public String nowText() {
        return LocalDateTime.now().toString();
    }
}
