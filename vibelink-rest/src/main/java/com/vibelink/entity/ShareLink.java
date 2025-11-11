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
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;  // 외부 공유 코드 (UUID 문자열)

    @Column(nullable = false)
    private String inviterSpotifyUserId;  // 초대자의 Spotify ID

    private Instant createdAt;
}
