package com.vibelink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spotify API 통신용 WebClient 구성 클래스
 */
@Configuration
public class SpotifyConfig {

    /**
     * Spotify 인증 서버 (accounts.spotify.com)
     * Access Token 발급 및 Refresh Token 재발급용
     */
    @Bean
    public WebClient spotifyAccountsClient() {
        return WebClient.builder()
                .baseUrl("https://accounts.spotify.com")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }

    /**
     * Spotify 일반 API 서버 (api.spotify.com/v1)
     * 사용자 프로필, 아티스트, 트랙 정보 조회용
     */
    @Bean
    @Primary
    public WebClient spotifyApiClient() {
        return WebClient.builder()
                .baseUrl("https://api.spotify.com")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                        .build())
                .build();
    }
}
