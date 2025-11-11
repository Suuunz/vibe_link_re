package com.vibelink.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcceptShareRequest {
    private String createPlaylistOn;  // "inviter" | "receiver"
    private Integer takePerUser;      // 각 사용자당 가져올 트랙 수
}
