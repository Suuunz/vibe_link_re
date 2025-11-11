package com.vibelink.controller;


import com.vibelink.entity.AppSession;
import com.vibelink.service.SpotifyAuthService;
import com.vibelink.service.SpotifyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final SpotifyAuthService authService;
    private final SpotifyDataService dataService;


    @Operation(summary = "Spotify 로그인 URL 생성")
    @GetMapping("/authorize")
    public Map<String, String> authorize(@RequestParam(value = "state", required = false) String state) {
        return Map.of("authorizeUrl", authService.buildAuthorizeUrl(state));
    }


    @Operation(summary = "Spotify OAuth 콜백")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> callback(@RequestParam("code") String code, HttpServletResponse resp) {
        Map<String, Object> token = authService.exchangeCodeForToken(code);
        String accessToken = (String) token.get("access_token");
        String refreshToken = (String) token.get("refresh_token");
        Number expiresIn = (Number) token.getOrDefault("expires_in", 3600);


        var me = dataService.getProfile(accessToken);
        String spotifyUserId = me.getId();


        AppSession session = authService.createOrUpdateSession(spotifyUserId, accessToken, refreshToken, expiresIn.longValue());


        return Map.of(
                "appToken", session.getAppToken(),
                "spotifyUserId", spotifyUserId,
                "expiresAt", session.getAccessTokenExpiresAt()
        );
    }
}