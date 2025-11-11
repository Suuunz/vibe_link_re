package com.vibelink.service;

import com.vibelink.dto.ArtistDto;
import com.vibelink.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.vibelink.dto.ProfileDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spotify 사용자 데이터(Top Artists, Tracks, Playlist) 조회 서비스
 */
@Service
@RequiredArgsConstructor
public class SpotifyDataService {

    private final @Qualifier("spotifyApiClient") WebClient apiClient;

    /**
     * Spotify Top Artists 조회
     */
    public List<ArtistDto> getTopArtists(String accessToken, int limit, String timeRange) {
        Map<String, Object> resp = apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/top/artists")
                        .queryParam("limit", limit)
                        .queryParam("time_range", timeRange)
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        return Mapper.mapArtists(items);
    }

    /**
     * 아티스트별 Top Tracks 조회
     */
    public List<String> getTopTracksByArtists(String accessToken, List<String> artistIds, int take) {
        return artistIds.stream().flatMap(id -> {
            Map<String, Object> resp = apiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/artists/{id}/top-tracks")
                            .queryParam("market", "KR")
                            .build(id))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> tracks = (List<Map<String, Object>>) resp.get("tracks");
            return tracks.stream().limit(take)
                    .map(t -> (String) t.get("uri"));
        }).collect(Collectors.toList());
    }

    /**
     * 새 Playlist 생성
     */
    public String createPlaylist(String accessToken, String userId, String name, String desc, boolean isPublic) {
        Map<String, Object> resp = apiClient.post()
                .uri("/users/{id}/playlists", userId)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(Map.of(
                        "name", name,
                        "description", desc,
                        "public", isPublic
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return (String) resp.get("id");
    }

    /**
     * 플레이리스트에 트랙 추가
     */
    public void addTracksToPlaylist(String accessToken, String playlistId, List<String> uris) {
        apiClient.post()
                .uri("/playlists/{id}/tracks", playlistId)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(Map.of("uris", uris))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public ProfileDto getProfile(String accessToken) {
        Map<String, Object> map = apiClient.get()
                .uri("/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (map == null) {
            throw new RuntimeException("Failed to fetch Spotify profile");
        }

        return ProfileDto.builder()
                .id((String) map.get("id"))
                .displayName((String) map.get("display_name"))
                .email((String) map.get("email"))
                .country((String) map.get("country"))
                .followers(map.get("followers") instanceof Map ? (Integer) ((Map<?, ?>) map.get("followers")).get("total") : 0)
                .imageUrl(extractImageUrl(map))
                .build();
    }
    private String extractImageUrl(Map<String, Object> map) {
        if (map.get("images") instanceof java.util.List<?> images && !images.isEmpty()) {
            var first = (Map<?, ?>) images.get(0);
            return (String) first.get("url");
        }
        return null;
    }
}
