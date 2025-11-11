package com.vibelink.util;

import com.vibelink.dto.ArtistDto;
import com.vibelink.dto.ProfileDto;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mapper {

    /**
     * Spotify 사용자 정보(JSON) → ProfileDto 매핑
     */
    public static ProfileDto mapProfile(Map<String, Object> me) {
        String id = (String) me.get("id");
        String displayName = (String) me.getOrDefault("display_name", id);
        String email = (String) me.get("email");

        Map<String, Object> followers = (Map<String, Object>) me.get("followers");
        Integer count = followers != null
                ? ((Number) followers.getOrDefault("total", 0)).intValue()
                : 0;

        String imageUrl = null;
        List<Map<String, Object>> images = (List<Map<String, Object>>) me.get("images");
        if (!CollectionUtils.isEmpty(images)) {
            imageUrl = (String) images.get(0).get("url");
        }

        return ProfileDto.builder()
                .id(id)
                .displayName(displayName)
                .email(email)
                .imageUrl(imageUrl)
                .followers(count)
                .build();
    }

    /**
     * Spotify 아티스트 목록(JSON) → ArtistDto 리스트 매핑
     */
    public static List<ArtistDto> mapArtists(List<Map<String, Object>> items) {
        List<ArtistDto> list = items.stream().map(a -> {
            String id = (String) a.get("id");
            String name = (String) a.get("name");
            Integer popularity = ((Number) a.getOrDefault("popularity", 0)).intValue();
            List<String> genres = (List<String>) a.get("genres");

            String imageUrl = null;
            List<Map<String, Object>> images = (List<Map<String, Object>>) a.get("images");
            if (!CollectionUtils.isEmpty(images)) {
                imageUrl = (String) images.get(0).get("url");
            }

            return ArtistDto.builder()
                    .id(id)
                    .name(name)
                    .popularity(popularity)
                    .genres(genres)
                    .imageUrl(imageUrl)
                    .build();
        }).collect(Collectors.toList());

        // 인기도 순으로 정렬 후 rank 부여
        list.sort(Comparator.comparing(
                ArtistDto::getPopularity,
                Comparator.nullsLast(Integer::compareTo)
        ).reversed());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        return list;
    }
}
