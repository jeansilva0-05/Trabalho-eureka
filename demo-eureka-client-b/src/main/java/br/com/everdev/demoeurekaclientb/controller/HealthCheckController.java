package br.com.everdev.demoeurekaclientb.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Applications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
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

    @PostMapping("/receiveCall/{name}")
    public String receiveCall(@PathVariable String name, @RequestBody String message) {
        return  message + "\nOlá " + name + ". Aqui é "+appName+" e recebi sua mensagem.";
    }

    @GetMapping("/makeCall/{name}")
    public String makeCall(@PathVariable String name) throws URISyntaxException {
        String message = "Olá, tem alguem ai??";

        List<InstanceInfo> instances = eurekaClient.getInstancesById(name);

        InstanceInfo instance = instances.getFirst();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://"+instance.getIPAddr() + ":" + instance.getPort()+"/receiveCall/"+appName))
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/callAppC")
    public String callAppC() {
        try {
            String serviceNameC = "demo-eureka-client-c";
            List<InstanceInfo> instancesC = eurekaClient.getInstancesByVipAddress(serviceNameC, false);
    
            if (instancesC.isEmpty()) {
                return "-2"; 
            }
    
            InstanceInfo instanceC = instancesC.get(0);
            String urlToCallC = "http://" + instanceC.getIPAddr() + ":" + instanceC.getPort() + "/sum";
    
            HttpRequest request = HttpRequest.newBuilder(new URI(urlToCallC))
                    .GET()
                    .build();
    
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    
            int resultFromC = Integer.parseInt(response.body().replaceAll("[\\D]", ""));
            int localRandom = new Random().nextInt(100); // Ensure you have a Random instance defined somewhere or just create it here
            int sum = resultFromC + localRandom;
    
            return "Resultado da soma: " + sum;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao chamar o APP C";
        }
    }
}    