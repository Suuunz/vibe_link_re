package com.vibelink.service;

import com.vibelink.dto.ArtistDto;
import com.vibelink.dto.BlendResultDto;
import com.vibelink.entity.AppSession;
import com.vibelink.entity.ShareLink;
import com.vibelink.exception.NotFoundException;
import com.vibelink.repository.AppSessionRepository;
import com.vibelink.repository.ShareLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BlendService {

    private final ShareLinkRepository shareLinkRepo;
    private final AppSessionRepository sessionRepo;
    private final SpotifyDataService dataService;

    public ShareLink createShareLink(AppSession inviterSession) {
        ShareLink link = ShareLink.builder()
                .code(UUID.randomUUID().toString())
                .inviterSpotifyUserId(inviterSession.getSpotifyUserId())
                .createdAt(java.time.Instant.now())
                .build();
        return shareLinkRepo.save(link);
    }

    public BlendResultDto computeBlend(String code, AppSession receiverSession, String createPlaylistOn, Integer takePerUser) {
        ShareLink link = shareLinkRepo.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Invalid share code"));
        AppSession inviterSession = sessionRepo.findBySpotifyUserId(link.getInviterSpotifyUserId())
                .orElseThrow(() -> new NotFoundException("Inviter session not found"));

        var inviterTop = dataService.getTopArtists(inviterSession.getAccessToken(), 20, "medium_term");
        var receiverTop = dataService.getTopArtists(receiverSession.getAccessToken(), 20, "medium_term");

        Set<String> inviterSet = inviterTop.stream().map(ArtistDto::getId).collect(Collectors.toSet());
        Set<String> receiverSet = receiverTop.stream().map(ArtistDto::getId).collect(Collectors.toSet());
        Set<String> common = new HashSet<>(inviterSet);
        common.retainAll(receiverSet);

        double overlap = common.isEmpty() ? 0.0 :
                (double) common.size() / (double) Math.max(inviterSet.size(), receiverSet.size());
        List<String> commonNames = inviterTop.stream()
                .filter(a -> common.contains(a.getId()))
                .map(ArtistDto::getName).collect(Collectors.toList());

        List<String> mergedArtistIds = Stream.concat(
                        inviterTop.stream(),
                        receiverTop.stream()
                )
                .map(ArtistDto::getId)
                .distinct()
                .limit(30)
                .collect(Collectors.toList());


        final int take = (takePerUser == null || takePerUser < 1) ? 2 : takePerUser;
        List<String> uris = dataService.getTopTracksByArtists(inviterSession.getAccessToken(), mergedArtistIds, take);

        String playlistId = null;
        String playlistUrl = null;
        if (!uris.isEmpty()) {
            boolean createOnInviter = !"receiver".equalsIgnoreCase(createPlaylistOn);
            AppSession owner = createOnInviter ? inviterSession : receiverSession;
            String plId = dataService.createPlaylist(owner.getAccessToken(), owner.getSpotifyUserId(),
                    "VibeLink Blend", "Auto-generated blend playlist", true);
            dataService.addTracksToPlaylist(owner.getAccessToken(), plId, uris);
            playlistId = plId;
            playlistUrl = "https://open.spotify.com/playlist/" + plId;
        }

        return BlendResultDto.builder()
                .inviterId(inviterSession.getSpotifyUserId())
                .receiverId(receiverSession.getSpotifyUserId())
                .overlapRatio(overlap)
                .commonArtistNames(commonNames)
                .playlistId(playlistId)
                .playlistUrl(playlistUrl)
                .build();
    }
}
