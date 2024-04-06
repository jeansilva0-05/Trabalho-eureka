package br.com.everdev.demoeurekaclientc.controller;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Applications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Random;

@RestController
public class HealthCheckController {

    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    private final Random random = new Random();

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/health")
    public String healthy() {
        return "Estpu vivo e bem! Sou a app "+appName+" - " + LocalDateTime.now();
    }

    @GetMapping("/discover")
    public String discover() {
        Applications otherApps = eurekaClient.getApplications();
        return otherApps.getRegisteredApplications().toString();
    }

    @RestController
    public class AppCController {
    
        @Value("${spring.application.name}")
        private String appName;
    
        private final Random random = new Random();
    
        @GetMapping("/sum")
        public String sum() {
            int result = random.nextInt(100);
            return "Número aleatório: " + result;
        }
    }
}
