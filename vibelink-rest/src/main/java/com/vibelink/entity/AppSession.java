package com.vibelink.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;  // 내부 식별자

    @Column(unique = true, nullable = false)
    private String appToken;  // 프론트엔드가 사용하는 세션 토큰 (X-App-Token)

    @Column(nullable = false)
    private String spotifyUserId;  // Spotify 계정 ID

    @Column(length = 2000)
    private String accessToken;

    @Column(length = 2000)
    private String refreshToken;

    private Instant accessTokenExpiresAt;  // 액세스 토큰 만료 시각

    private Instant createdAt;
}
