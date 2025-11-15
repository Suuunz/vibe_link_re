package com.vibelink.service;

import com.vibelink.entity.AppSession;
import com.vibelink.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpotifyAuthService {

    private final WebClient accountsClient;
    private final WebClient apiClient;
    private final AppSessionRepository sessionRepo;

    public SpotifyAuthService(
            @Qualifier("spotifyAccountsClient") WebClient accountsClient,
            @Qualifier("spotifyApiClient") WebClient apiClient,
            AppSessionRepository sessionRepo
    ) {
        this.accountsClient = accountsClient;
        this.apiClient = apiClient;
        this.sessionRepo = sessionRepo;
    }

    @Value("${app.spotify.client-id}")
    private String clientId;

    @Value("${app.spotify.client-secret}")
    private String clientSecret;

    @Value("${app.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${app.spotify.scope:user-read-email user-top-read playlist-modify-public playlist-modify-private}")
    private String scope;

    // -------------------------------------------------------------
    // 1) Spotify 로그인 URL 생성
    // -------------------------------------------------------------
    public String buildAuthorizeUrl(String state) {
        String encodedScope = java.net.URLEncoder.encode(scope, java.nio.charset.StandardCharsets.UTF_8);
        String encodedRedirect = java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8);

        String url = String.format(
                "https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=%s",
                clientId,
                encodedRedirect,
                encodedScope
        );

        if (state != null && !state.isBlank()) {
            url += "&state=" + java.net.URLEncoder.encode(state, java.nio.charset.StandardCharsets.UTF_8);
        }

        return url;
    }

    // -------------------------------------------------------------
    // 2) Authorization Code → Access Token 교환  (✔ FIXED)
    // -------------------------------------------------------------
    public Map<String, Object> exchangeCodeForToken(String code) {

        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        // Spotify는 반드시 Form-Data 형식으로 받아야 함
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        return accountsClient.post()
                .uri("/api/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // -------------------------------------------------------------
    // 3) 세션 저장 / 업데이트
    // -------------------------------------------------------------
    public AppSession createOrUpdateSession(String spotifyUserId,
                                            String accessToken,
                                            String refreshToken,
                                            long expiresInSeconds) {

        AppSession session = sessionRepo.findBySpotifyUserId(spotifyUserId)
                .orElseGet(() -> AppSession.builder()
                        .id(null)
                        .spotifyUserId(spotifyUserId)
                        .appToken(UUID.randomUUID().toString())
                        .createdAt(Instant.now())
                        .build());

        session.setAccessToken(accessToken);

        if (refreshToken != null && !refreshToken.isBlank()) {
            session.setRefreshToken(refreshToken);
        }

        session.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresInSeconds));

        return sessionRepo.save(session);
    }

    // -------------------------------------------------------------
    // 4) Refresh Token → Access Token 갱신  (✔ FIXED)
    // -------------------------------------------------------------
    public String refreshAccessToken(AppSession session) {

        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", session.getRefreshToken());

        Map<String, Object> resp = accountsClient.post()
                .uri("/api/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String newAccessToken = (String) resp.get("access_token");
        Number expiresIn = (Number) resp.getOrDefault("expires_in", 3600);

        session.setAccessToken(newAccessToken);
        session.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn.longValue()));
        sessionRepo.save(session);

        return newAccessToken;
    }

    // -------------------------------------------------------------
    // 세션 조회 유틸
    // -------------------------------------------------------------
    public Optional<AppSession> findByAppToken(String appToken) {
        return sessionRepo.findByAppToken(appToken);
    }

    public AppSession requireSession(String appToken) {
        return sessionRepo.findByAppToken(appToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired app token"));
    }
}

