package no.sysco.cip;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpRunner implements CommandLineRunner {


    private final RestTemplate restTemplate;

    public HttpRunner(RestTemplate restTemplate) {this.restTemplate = restTemplate;}

    @Override
    public void run(String... args) {
        var response = this.restTemplate.getForEntity("http://localhost:8080/hello/world", String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("ERROR >>>>>>");
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
        }
        System.out.println("Success >>>>>>");
        System.out.println(response.getBody());
    }
}
