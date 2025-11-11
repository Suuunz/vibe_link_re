package com.vibelink.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistDto {
    private String id;
    private String name;
    private Integer popularity;  // 0 ~ 100
    private List<String> genres;
    private String imageUrl;
    private Integer rank;        // 순위 (1부터)
}
