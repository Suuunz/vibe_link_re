package com.vibelink.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateShareLinkResponse {
    private String code;      // 공유 코드
    private String shareUrl;  // ex) https://yourfrontend.com/share/{code}
}
