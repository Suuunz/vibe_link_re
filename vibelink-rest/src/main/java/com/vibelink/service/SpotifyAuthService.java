package com.vibelink.service;

import com.vibelink.entity.AppSession;
import com.vibelink.repository.AppSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.nio.charset.StandardCharsets;
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

    // ⚠️ application.yml의 키와 반드시 맞추세요. (app.spotify.*)
    @Value("${app.spotify.client-id}")
    private String clientId;

    @Value("${app.spotify.client-secret}")
    private String clientSecret;

    @Value("${app.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${app.spotify.scope:user-read-email user-top-read playlist-modify-public playlist-modify-private}")
    private String scope;



    // ---------------------------------------------
    // 1) 로그인 URL 생성
    // ---------------------------------------------


    public String buildAuthorizeUrl(String state) {
        try {
            String scope = URLEncoder.encode(
                    "user-read-email user-top-read playlist-modify-public playlist-modify-private",
                    StandardCharsets.UTF_8
            );

            String url = String.format(
                    "https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=%s",
                    clientId,
                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                    scope
            );

            if (state != null && !state.isBlank()) {
                url += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
            }

            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build authorize URL", e);
        }
    }

    // ---------------------------------------------
    // 2) Authorization Code → Token 교환 (Map 반환)
    //    AuthController에서 그대로 사용
    // ---------------------------------------------
    public Map<String, Object> exchangeCodeForToken(String code) {
        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return accountsClient.post()
                .uri("/api/token")
                .header("Authorization", "Basic " + basicAuth)
                .bodyValue(Map.of(
                        "grant_type", "authorization_code",
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ---------------------------------------------
    // 3) 세션 생성/업데이트 (AuthController에서 사용)
    // ---------------------------------------------
    public AppSession createOrUpdateSession(String spotifyUserId,
                                            String accessToken,
                                            String refreshToken,
                                            long expiresInSeconds) {
        AppSession session = sessionRepo.findBySpotifyUserId(spotifyUserId)
                .orElseGet(() -> AppSession.builder()
                        .id(null) // JPA @GeneratedValue
                        .appToken(UUID.randomUUID().toString())
                        .spotifyUserId(spotifyUserId)
                        .createdAt(Instant.now())
                        .build());

        session.setAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            session.setRefreshToken(refreshToken);
        }
        session.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresInSeconds));

        return sessionRepo.save(session);
    }

    // ---------------------------------------------
    // (옵션) 만료 시 토큰 갱신
    // ---------------------------------------------
    public String refreshAccessToken(AppSession session) {
        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> resp = accountsClient.post()
                .uri("/api/token")
                .header("Authorization", "Basic " + basicAuth)
                .bodyValue(Map.of(
                        "grant_type", "refresh_token",
                        "refresh_token", session.getRefreshToken()
                ))
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

    // ---------------------------------------------
    // 세션 조회/검증 유틸
    // ---------------------------------------------
    public Optional<AppSession> findByAppToken(String appToken) {
        return sessionRepo.findByAppToken(appToken);
    }

    public AppSession requireSession(String appToken) {
        return sessionRepo.findByAppToken(appToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired app token"));
    }
}
