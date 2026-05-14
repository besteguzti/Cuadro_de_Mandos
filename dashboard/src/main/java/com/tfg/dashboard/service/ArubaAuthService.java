package com.tfg.dashboard.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.tfg.dashboard.model.OAuthToken;
import com.tfg.dashboard.repository.OAuthTokenRepository;

@Service
public class ArubaAuthService {

    @Value("${aruba.client.id}")
    private String clientId;

    @Value("${aruba.client.secret}")
    private String clientSecret;

    @Value("${aruba.base.url}")
    private String baseUrl;

    private final OAuthTokenRepository tokenRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public ArubaAuthService(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String getAccessToken() {

        OAuthToken token = tokenRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe token OAuth en MySQL"));

        long now = System.currentTimeMillis();

        // token aún válido
        if (token.getExpiresAt() != null
                && now < token.getExpiresAt()) {

            return token.getAccessToken();
        }

        System.out.println("Renovando token Aruba...");

        String url = baseUrl + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("grant_type", "refresh_token");
        body.add("refresh_token", token.getRefreshToken());
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        Map.class);

        Map<String, Object> responseBody =
                response.getBody();

        String newAccessToken =
                (String) responseBody.get("access_token");

        String newRefreshToken =
                (String) responseBody.get("refresh_token");

        Integer expiresIn =
                (Integer) responseBody.get("expires_in");

        token.setAccessToken(newAccessToken);

        if (newRefreshToken != null) {
            token.setRefreshToken(newRefreshToken);
        }

        token.setExpiresAt(
                now + ((expiresIn - 60) * 1000L));

        tokenRepository.save(token);

        return newAccessToken;
    }
}