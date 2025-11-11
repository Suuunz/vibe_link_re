package com.vibelink.repository;

import com.vibelink.entity.AppSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AppSessionRepository extends JpaRepository<AppSession, UUID> {

    Optional<AppSession> findByAppToken(String appToken);

    Optional<AppSession> findBySpotifyUserId(String spotifyUserId);
}
