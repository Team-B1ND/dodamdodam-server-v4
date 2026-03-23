# dodam-dauth

도담도담 OAuth 2.0 인증 웹 — 외부 서비스가 도담도담 계정으로 로그인하고, 권한 동의 후 API를 사용할 수 있게 하는 OAuth Provider 프론트엔드.

---

## 프로젝트 정보

- **이름**: dodam-dauth
- **기술 스택**: Next.js 14 (App Router) + TypeScript + axios
- **스타일**: Tailwind CSS
- **패키지 매니저**: pnpm
- **포트**: 3000

---

## 환경

| 서비스 | URL (로컬) | URL (운영) |
|--------|------------|------------|
| Gateway | `http://localhost:8080` | `https://dodam.b1nd.com` |
| dodam-dauth | `http://localhost:3000` | `https://dauth.dodam.b1nd.com` |

환경변수:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_DAUTH_URL=http://localhost:3000
```

---

## 라우트 구조 (App Router)

```
app/
├── layout.tsx                  → 공통 레이아웃
├── page.tsx                    → 랜딩 (서비스 소개)
├── login/
│   └── page.tsx                → 도담 로그인 페이지
├── authorize/
│   └── page.tsx                → OAuth 동의 화면
├── callback/
│   └── page.tsx                → Authorization code → 토큰 교환
├── register/
│   └── page.tsx                → 클라이언트(앱) 등록
├── dashboard/
│   ├── page.tsx                → 내 앱 관리 (등록된 클라이언트 조회)
│   └── [clientId]/
│       └── page.tsx            → 앱 상세 (수정, secret 재발급, 비활성화)
└── playground/
    └── page.tsx                → API 테스트 (토큰으로 API 호출)
```

---

## 전체 플로우

### A. 외부 개발자가 앱을 등록하는 플로우

```
[/register]
  → 도담 로그인 필요
  → 앱 이름, redirect URI, scope 선택
  → POST /oauth/clients
  → clientId + clientSecret 발급 (최초 1회만 노출)
  → /dashboard 로 이동
```

### B. 외부 서비스가 OAuth 로그인을 요청하는 플로우

```
[외부 서비스]
  → 브라우저를 {DAUTH_URL}/authorize?client_id=...&redirect_uri=...&scope=...&state=...&code_challenge=...&code_challenge_method=S256 로 redirect

[/authorize]
  1. query에서 client_id, redirect_uri, scope, state, code_challenge 파싱
  2. GET /oauth/authorize 호출 → 클라이언트 정보 + scope 검증
  3. 유저가 미로그인 → /login?next=/authorize?... 로 redirect
  4. 로그인 상태면 동의 화면 렌더링 (앱 이름, 요청 scope 표시)
  5. "허용" → POST /oauth/authorize/consent (도담 토큰으로 인증)
  6. 응답의 redirectUri로 브라우저 redirect (외부 서비스 callback)

[/login]
  → 도담 username/password 로그인
  → POST /auth/login → access token 획득
  → cookie 또는 sessionStorage에 저장
  → next 파라미터가 있으면 해당 경로로 redirect
```

### C. Playground (API 테스트)

```
[/playground]
  → 저장된 OAuth 토큰으로 API 호출 테스트
  → 직접 토큰 입력도 가능
  → 요청/응답 JSON 표시
```

---

## API 상세

### 1. 도담 로그인

```
POST {API_URL}/auth/login
Content-Type: application/json

{
  "username": "아이디",
  "password": "비밀번호"
}

Response 200:
{
  "status": 200,
  "message": "...",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

### 2. 클라이언트 등록

```
POST {API_URL}/oauth/clients
Content-Type: application/json

{
  "clientName": "우리 학교 앱",
  "redirectUris": ["https://myapp.com/callback"],
  "scopes": ["meal:read", "profile:read"],
  "websiteUrl": "https://myapp.com",
  "description": "학생들을 위한 편의 앱",
  "logoUrl": "https://myapp.com/logo.png"
}

Response 201:
{
  "status": 201,
  "message": "Client registered",
  "data": {
    "clientId": "dodam_a1b2c3d4e5f6",
    "clientSecret": "dcs_xxxxxxxxxxxx",
    "clientName": "우리 학교 앱",
    "redirectUris": ["https://myapp.com/callback"],
    "scopes": ["meal:read", "profile:read"],
    "websiteUrl": "https://myapp.com",
    "description": "학생들을 위한 편의 앱",
    "logoUrl": "https://myapp.com/logo.png",
    "createdAt": "2025-03-21T00:00:00"
  }
}
```

⚠️ `clientSecret`은 이 응답에서만 평문 노출. 이후 재조회 불가.

### 3. 클라이언트 조회

```
GET {API_URL}/oauth/clients/{clientId}

Response 200:
{
  "status": 200,
  "message": "Client found",
  "data": {
    "clientId": "dodam_a1b2c3d4e5f6",
    "clientName": "우리 학교 앱",
    "redirectUris": ["https://myapp.com/callback"],
    "scopes": ["meal:read", "profile:read"],
    "websiteUrl": "https://myapp.com",
    "description": "학생들을 위한 편의 앱",
    "logoUrl": "https://myapp.com/logo.png",
    "createdAt": "2025-03-21T00:00:00"
  }
}
```

clientSecret은 조회 시 포함되지 않는다.

### 4. 클라이언트 수정

```
PUT {API_URL}/oauth/clients/{clientId}
Authorization: Basic {base64(clientId:clientSecret)}
Content-Type: application/json

{
  "clientName": "수정된 앱 이름",
  "redirectUris": ["https://myapp.com/callback", "https://myapp.com/auth"],
  "scopes": ["meal:read", "profile:read", "outgoing:read"],
  "websiteUrl": "https://myapp.com",
  "description": "수정된 설명"
}

Response 200:
{
  "status": 200,
  "message": "Client updated",
  "data": { ... }
}
```

### 5. 클라이언트 비활성화

```
DELETE {API_URL}/oauth/clients/{clientId}
Authorization: Basic {base64(clientId:clientSecret)}

Response 200:
{
  "status": 200,
  "message": "Client deactivated"
}
```

### 6. Secret 재발급

```
POST {API_URL}/oauth/clients/{clientId}/secret/reset
Authorization: Basic {base64(clientId:clientSecret)}

Response 200:
{
  "status": 200,
  "message": "Secret reset",
  "data": {
    "clientId": "dodam_a1b2c3d4e5f6",
    "clientSecret": "dcs_new_secret_xxx",
    ...
  }
}
```

### 7. Scope 목록 조회

```
GET {API_URL}/oauth/clients/scopes

Response 200:
{
  "status": 200,
  "message": "Scopes found",
  "data": [
    { "scopeKey": "meal:read", "description": "급식 정보 조회" },
    { "scopeKey": "nightstudy:read", "description": "야간 자율학습 정보 조회" },
    { "scopeKey": "nightstudy:write", "description": "야간 자율학습 신청/취소" },
    { "scopeKey": "outgoing:read", "description": "외출/외박 정보 조회" },
    { "scopeKey": "outgoing:write", "description": "외출/외박 신청/취소" },
    { "scopeKey": "wakeupsong:read", "description": "기상송 정보 조회" },
    { "scopeKey": "wakeupsong:write", "description": "기상송 신청" },
    { "scopeKey": "profile:read", "description": "기본 프로필 조회 (이름, 학번)" }
  ]
}
```

### 8. Authorization 요청

```
GET {API_URL}/oauth/authorize
  ?response_type=code
  &client_id={clientId}
  &redirect_uri={redirectUri}
  &scope=meal:read profile:read
  &state={랜덤문자열}
  &code_challenge={BASE64URL(SHA256(code_verifier))}
  &code_challenge_method=S256

Response 200:
{
  "status": 200,
  "message": "Authorization request validated",
  "data": {
    "clientName": "우리 학교 앱",
    "clientId": "dodam_a1b2c3d4e5f6",
    "scopes": [
      { "scopeKey": "meal:read", "description": "급식 정보 조회" },
      { "scopeKey": "profile:read", "description": "기본 프로필 조회 (이름, 학번)" }
    ],
    "redirectUri": "https://myapp.com/callback",
    "state": "...",
    "codeChallenge": "...",
    "codeChallengeMethod": "S256"
  }
}
```

### 9. 동의 처리

```
POST {API_URL}/oauth/authorize/consent
Authorization: Bearer {도담_access_token}
Content-Type: application/json

{
  "clientId": "dodam_a1b2c3d4e5f6",
  "redirectUri": "https://myapp.com/callback",
  "scope": "meal:read profile:read",
  "state": "{state}",
  "codeChallenge": "{code_challenge}",
  "codeChallengeMethod": "S256",
  "approved": true
}

Response 200:
{
  "status": 200,
  "message": "Consent processed",
  "data": {
    "redirectUri": "https://myapp.com/callback?code=abc123...&state=xyz"
  }
}
```

거부 시 `"approved": false` → redirectUri에 `?error=access_denied&state=xyz`

### 10. 토큰 교환

```
POST {API_URL}/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code={code}
&redirect_uri={redirect_uri}
&client_id={clientId}
&client_secret={clientSecret}
&code_verifier={code_verifier}

Response 200 (RFC 표준, Response<T> 래퍼 아님):
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 180,
  "refresh_token": "drt_xxxxxxxxxxxx",
  "scope": "meal:read profile:read"
}
```

### 11. 토큰 갱신

```
POST {API_URL}/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token={refresh_token}
&client_id={clientId}
&client_secret={clientSecret}

Response 200: (토큰 교환과 동일)
```

### 12. 토큰 폐기

```
POST {API_URL}/oauth/token/revoke
Content-Type: application/x-www-form-urlencoded

token={access_token 또는 refresh_token}

Response 200: (항상 200, body 없음)
```

### 13. OAuth 토큰으로 API 호출

```
GET {API_URL}/meal/today
Authorization: Bearer {oauth_access_token}

→ 200: 정상 응답
→ 403: scope 부족 (Forbidden)
→ 401: 토큰 만료/무효 (Unauthorized)
```

### 14. Well-known

```
GET {API_URL}/.well-known/openid-configuration
GET {API_URL}/oauth/.well-known/jwks.json
```

---

## PKCE 구현 (필수)

SPA이므로 PKCE (RFC 7636)를 사용한다:

```typescript
// lib/pkce.ts

export function generateCodeVerifier(): string {
  const array = new Uint8Array(64);
  crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...array))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

export async function generateCodeChallenge(verifier: string): Promise<string> {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

export function generateState(): string {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return Array.from(array, (b) => b.toString(16).padStart(2, '0')).join('');
}
```

---

## 클라이언트 저장소 전략

| key | 저장소 | 설명 |
|-----|--------|------|
| `dauth_dodam_token` | cookie (httpOnly X, SameSite=Lax) | 도담 내부 access token |
| `dauth_client_id` | localStorage | 등록한 앱의 clientId |
| `dauth_client_secret` | localStorage | 등록한 앱의 clientSecret |
| `dauth_code_verifier` | sessionStorage | PKCE code_verifier (일회성) |
| `dauth_state` | sessionStorage | CSRF 방지 state (일회성) |
| `dauth_oauth_token` | sessionStorage | OAuth access token (playground용) |
| `dauth_refresh_token` | sessionStorage | OAuth refresh token (playground용) |

---

## 페이지별 상세

### `/` — 랜딩

- 서비스 소개: "도담도담 OAuth로 학교 API를 연동하세요"
- "앱 등록하기" → `/register`
- "개발자 문서" → well-known 엔드포인트 안내

### `/login` — 도담 로그인

- username, password 입력 폼
- `POST /auth/login` 호출
- 성공 시 `dauth_dodam_token` 저장
- `next` query param이 있으면 해당 경로로 redirect, 없으면 `/` 로 이동
- 이미 로그인 상태면 바로 redirect

### `/register` — 앱 등록

- 도담 로그인 필수 (미로그인 시 `/login?next=/register`로 redirect)
- 폼:
  - 앱 이름 (필수, 2~100자)
  - 설명 (선택)
  - 웹사이트 URL (선택)
  - 로고 URL (선택)
  - Redirect URI 입력 (여러 개 추가 가능, HTTPS 필수 — localhost 예외)
  - Scope 체크박스 (GET /oauth/clients/scopes 에서 로드)
- 등록 성공 시:
  - clientId, clientSecret을 **모달**로 표시
  - "clientSecret은 다시 볼 수 없습니다. 반드시 복사하세요" 경고
  - 복사 버튼 제공
  - localStorage에 저장

### `/authorize` — OAuth 동의 화면

이 페이지는 **외부 서비스가 유저를 redirect시켜서 도착하는 페이지**다.

URL 예시:
```
/authorize?client_id=dodam_xxx&redirect_uri=https://myapp.com/callback&scope=meal:read%20profile:read&state=abc&code_challenge=xxx&code_challenge_method=S256
```

동작:
1. query에서 파라미터 파싱
2. 도담 로그인 확인 → 미로그인 시 `/login?next={현재 URL 전체}` 로 redirect
3. `GET /oauth/authorize` 호출 (파라미터 검증)
4. 성공 시 동의 화면 렌더링:
   - 앱 이름, 앱 로고
   - 요청하는 권한 목록 (scope 한국어 설명)
   - "허용" / "거부" 버튼
5. "허용" → `POST /oauth/authorize/consent` (도담 토큰으로 인증)
6. 응답의 `data.redirectUri`로 `window.location.href` 변경
7. "거부" → `approved: false`로 consent 호출 → error redirect

### `/callback` — 토큰 교환 (Playground용)

Playground에서 자체 테스트할 때 사용하는 callback 페이지.

1. URL에서 `code`, `state` 파싱
2. `error` param이 있으면 에러 표시
3. sessionStorage의 `dauth_state`와 비교
4. `POST /oauth/token` 호출 (code + code_verifier + client credentials)
5. 성공 시 토큰 저장 → `/playground` 이동

### `/dashboard` — 내 앱 관리

- 도담 로그인 필수
- localStorage에서 등록된 clientId 로드
- `GET /oauth/clients/{clientId}`로 앱 정보 조회
- 앱 정보 표시 (이름, redirect URI, scope 등)
- clientId 표시 (복사 버튼)

### `/dashboard/[clientId]` — 앱 상세

- 앱 수정 (PUT /oauth/clients/{clientId})
- Secret 재발급 (POST /oauth/clients/{clientId}/secret/reset)
- 앱 비활성화 (DELETE /oauth/clients/{clientId})
- 모든 인증 요청에 Basic Auth (clientId:clientSecret) 필요

### `/playground` — API 테스트

- OAuth 토큰 입력 (직접 입력 또는 sessionStorage에서 로드)
- JWT payload 디코딩하여 표시 (sub, scope, exp, iat)
- expires_in 카운트다운 타이머
- API 테스트 버튼:
  - 급식 조회 (`GET /meal/today`) — `meal:read` 필요
  - 프로필 조회 (`GET /user/my`) — `profile:read` 필요
  - 기상송 조회 (`GET /wakeup-song/`) — `wakeupsong:read` 필요
- 요청/응답 JSON 패널 (syntax highlighting)
- 403 → "scope 부족" 표시
- 401 → "토큰 만료" + "토큰 갱신" 버튼
- 토큰 갱신 버튼 (refresh_token 사용)
- "OAuth 로그인 테스트" 버튼 → authorize 플로우 시작 (자체 callback 사용)

---

## axios 설정

```typescript
// lib/api.ts
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// 일반 API (OAuth 토큰 사용)
export const api = axios.create({ baseURL: API_URL });

// 도담 내부 API (도담 토큰 사용)
export const dodamApi = axios.create({ baseURL: API_URL });
```

---

## 에러 응답 형식

**OAuth 표준 API** (`/oauth/token`, `/oauth/token/revoke`, `/oauth/token/introspect`):
```json
{ "error": "invalid_grant", "error_description": "Authorization code has expired" }
```

**도담 래퍼 API** (`/oauth/clients`, `/oauth/authorize`, `/oauth/authorize/consent`):
```json
{ "status": 400, "message": "Requested scope is not allowed", "code": "INVALID_SCOPE" }
```

두 형식 모두 처리해야 한다.

---

## Next.js 설정

```typescript
// next.config.ts
const nextConfig = {
  async rewrites() {
    return [];  // CORS가 Gateway에서 허용되므로 proxy 불필요
  },
};
export default nextConfig;
```

Gateway CORS는 wildcard origin 허용 설정이 되어 있다.
