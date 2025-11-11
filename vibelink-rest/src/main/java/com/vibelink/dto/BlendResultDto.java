package com.vibelink.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlendResultDto {
    private String inviterId;
    private String receiverId;
    private double overlapRatio;           // 두 사용자 취향 겹침 비율
    private List<String> commonArtistNames;
    private String playlistId;
    private String playlistUrl;
}
