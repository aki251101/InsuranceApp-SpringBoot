package jp.yoshiaki.insuranceapp.service;

import org.springframework.stereotype.Component;

@Component
public class GreetingService {

    private final TimeProvider timeProvider;

    public GreetingService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public String greet(String name) {
        return "Hello, " + name + "! (" + timeProvider.nowText() + ")";
    }
}
