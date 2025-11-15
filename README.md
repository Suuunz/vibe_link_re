# 🎵 VibeLink REST API
Spotify api를 활용하여 spotify blend 기능을 목표로 만든 프로젝트의
스프링부트로 구성된 rest api 서버입니다.

---

## 🚀 주요 기능
- Spotify OAuth2 로그인 (`/api/auth/authorize`, `/api/auth/callback`)
- 사용자 프로필 및 Top Artists 조회 (`/api/me`, `/api/me/top-artists`)
- 취향 공유 링크 생성 및 초대 (`/api/blend/share`, `/api/blend/share/{code}/accept`)
- 두 사용자의 Top Artists 비교 및 공통 아티스트 기반 Playlist 생성
- Swagger UI를 통한 API 문서 자동화

---

## ⚙️ Swagger api 테스트 방법

### 1. Spotify Developer 설정
- [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)에서 새 앱 생성
- `client_id` / `client_secret` 발급
- Redirect URI에 **`http://localhost:8080/api/auth/callback`** 추가

### 2. 환경설정 (`src/main/resources/application.yml`)
```yaml
app:
  spotify:
    client-id: "YOUR_SPOTIFY_CLIENT_ID"
    client-secret: "YOUR_SPOTIFY_CLIENT_SECRET"
