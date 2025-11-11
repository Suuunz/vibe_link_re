package com.vibelink.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopArtistsResponse {
    private ProfileDto profile;
    private List<ArtistDto> topArtists;
    private Double averagePopularity;
}
