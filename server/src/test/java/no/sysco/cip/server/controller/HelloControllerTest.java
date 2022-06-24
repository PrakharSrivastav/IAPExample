package no.sysco.cip.server.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.auth.oauth2.ServiceAccountCredentials;
import no.sysco.cip.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Server.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HelloControllerTest {


    @Autowired
    TestRestTemplate testRestTemplate;

    @LocalServerPort
    String port;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should fail without authorization header")
    void hello_fail() {

        String url = String.format("http://localhost:%s/hello/world", this.port);

        ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET, null, String.class);

        Assertions.assertEquals(401, response.getStatusCodeValue());
    }


    @Test
    @DisplayName("should succeed with authorization header")
    void hello_success() throws IOException {
       // var res = this.getClass().getResourceAsStream("resources/cip-dev.json");

        ClassPathResource classPathResource = new ClassPathResource("cip-dev.json");


        String url = String.format("http://localhost:%s/hello/world", this.port);

        var credential = ServiceAccountCredentials.fromStream(classPathResource.getInputStream());
        var privateKey = credential.getPrivateKey();
        var privateKeyId = credential.getPrivateKeyId();
        var now = System.currentTimeMillis();
        var algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privateKey);

        var authToken = JWT.create()
            .withKeyId(privateKeyId)
            .withIssuer(credential.getClientEmail())
            .withSubject(credential.getClientEmail())
            .withAudience(credential.getTokenServerUri().toString())
            .withIssuedAt(new Date(now))
            .withExpiresAt(new Date(now + 3600 * 1000L))
            .sign(algorithm);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authToken);

        ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), String.class);

        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertTrue(response.getBody().contains("world"));
        Assertions.assertTrue(response.getBody().contains("dev-iap-user@cip-kubernetes-dev.iam.gserviceaccount.com"));
    }
}