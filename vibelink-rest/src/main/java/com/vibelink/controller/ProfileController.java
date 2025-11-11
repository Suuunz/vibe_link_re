package com.vibelink.controller;


import com.vibelink.dto.TopArtistsResponse;
import com.vibelink.service.SpotifyAuthService;
import com.vibelink.service.SpotifyDataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {


    private final SpotifyAuthService authService;
    private final SpotifyDataService dataService;


    @Operation(summary = "내 프로필 조회")
    @GetMapping
    public Object me(@RequestHeader("X-App-Token") String appToken) {
        var session = authService.requireSession(appToken);
        var profile = dataService.getProfile(session.getAccessToken());
        return profile;
    }


    @Operation(summary = "내 Top Artists 조회")
    @GetMapping("/top-artists")
    public TopArtistsResponse topArtists(@RequestHeader("X-App-Token") String appToken,
                                         @RequestParam(defaultValue = "10") int limit,
                                         @RequestParam(defaultValue = "medium_term") String timeRange) {
        var session = authService.requireSession(appToken);
        var profile = dataService.getProfile(session.getAccessToken());
        var artists = dataService.getTopArtists(session.getAccessToken(), limit, timeRange);
        double avg = artists.stream().mapToInt(a -> a.getPopularity() == null ? 0 : a.getPopularity()).average().orElse(0);
        return TopArtistsResponse.builder()
                .profile(profile)
                .topArtists(artists)
                .averagePopularity(avg)
                .build();
    }
}