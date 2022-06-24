package no.sysco.cip.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class ClientConfig {

    @Bean
    public ServiceAccountCredentials serviceAccountCredentials() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("cip-dev.json");
            return ServiceAccountCredentials.fromStream(classPathResource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().interceptors(((request, body, execution) -> {
            request.getHeaders().set(AUTHORIZATION, String.format("Bearer %s", this.token(false)));
            var response = execution.execute(request, body);
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                request.getHeaders().remove(AUTHORIZATION);
                request.getHeaders().set(AUTHORIZATION, String.format("Bearer %s", this.token(true)));
                return execution.execute(request, body);
            }
            return response;
        })).build();
    }

    private String authToken;

    private String newToken() {
        this.authToken = ""; // blank the auth token

        var credential = serviceAccountCredentials();
        var privateKey = credential.getPrivateKey();
        var privateKeyId = credential.getPrivateKeyId();
        var now = System.currentTimeMillis();
        var algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privateKey);

        this.authToken = JWT.create()
            .withKeyId(privateKeyId)
            .withIssuer(credential.getClientEmail())
            .withSubject(credential.getClientEmail())
            .withAudience(credential.getTokenServerUri().toString())
            .withIssuedAt(new Date(now))
            .withExpiresAt(new Date(now + 3600 * 1000L))
            .sign(algorithm);

        return this.authToken;
    }

    private String token(boolean forceRefresh) {
        if (forceRefresh) {
            return this.newToken();
        }

        if (this.authToken != null && !this.authToken.isEmpty()) {
            return this.authToken;
        }
        // if none of above then return a new token
        return this.newToken();
    }
}
