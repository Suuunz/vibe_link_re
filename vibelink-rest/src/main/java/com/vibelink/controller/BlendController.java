package com.vibelink.controller;


import com.vibelink.dto.AcceptShareRequest;
import com.vibelink.dto.BlendResultDto;
import com.vibelink.dto.CreateShareLinkResponse;
import com.vibelink.entity.ShareLink;
import com.vibelink.service.BlendService;
import com.vibelink.service.SpotifyAuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/blend")
@RequiredArgsConstructor
public class BlendController {


    private final BlendService blendService;
    private final SpotifyAuthService authService;


    @Value("${app.frontend.share-base:https://example.com/share}")
    private String shareBase;


    @Operation(summary = "Share 링크 생성")
    @PostMapping("/share")
    public CreateShareLinkResponse createShare(@RequestHeader("X-App-Token") String appToken) {
        var inviterSession = authService.requireSession(appToken);
        ShareLink link = blendService.createShareLink(inviterSession);
        return CreateShareLinkResponse.builder()
                .code(link.getCode())
                .shareUrl(shareBase + "/" + link.getCode())
                .build();
    }


    @Operation(summary = "초대 수락 및 Blend 생성")
    @PostMapping("/share/{code}/accept")
    public BlendResultDto accept(@RequestHeader("X-App-Token") String appToken,
                                 @PathVariable String code,
                                 @RequestBody(required = false) AcceptShareRequest req) {
        var receiverSession = authService.requireSession(appToken);
        String createOn = req != null ? req.getCreatePlaylistOn() : "inviter";
        Integer take = req != null ? req.getTakePerUser() : null;
        return blendService.computeBlend(code, receiverSession, createOn, take);
    }
}