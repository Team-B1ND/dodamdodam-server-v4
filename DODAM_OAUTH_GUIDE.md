# 도담도담 OAuth 2.0 서버 개발자 가이드

도담도담 OAuth 2.0 Provider를 사용하여 외부 서비스에서 도담도담 유저의 데이터에 접근하는 방법을 안내해요.

---

## 목차

1. [개요](#개요)
2. [사전 준비](#사전-준비)
3. [앱 등록](#앱-등록)
4. [OAuth 로그인 구현](#oauth-로그인-구현)
5. [토큰으로 API 호출](#토큰으로-api-호출)
6. [토큰 갱신](#토큰-갱신)
7. [토큰 폐기](#토큰-폐기)
8. [Token Exchange (인앱 WebView)](#token-exchange-인앱-webview)
9. [Scope 목록](#scope-목록)
10. [앱 관리 API](#앱-관리-api)
11. [에러 처리](#에러-처리)
12. [보안 주의사항](#보안-주의사항)
13. [언어별 예제](#언어별-예제)
14. [PKCE 구현 가이드](#pkce-구현-가이드)
15. [FAQ](#faq)

---

## 개요

도담도담 OAuth는 **OAuth 2.0 Authorization Code + PKCE** 방식을 지원해요.
외부 서비스가 도담도담 유저를 대신하여 외출, 기상송 등의 API를 호출할 수 있어요.

**지원 방식:**
- Authorization Code Grant (웹, 서버 사이드)
- Authorization Code + PKCE (SPA, 모바일)
- Token Exchange (인앱 WebView 전용)

**기본 URL:**
- 운영 API: `https://dodam.b1nd.com`
- 운영 인증: `https://dauth.dodam.b1nd.com`
- 로컬 API: `http://localhost:8080`
- 로컬 인증: `http://localhost:3000`

**디스커버리 엔드포인트:**
```
GET /.well-known/openid-configuration
```
이 엔드포인트를 호출하면 모든 엔드포인트 URL, 지원 scope, 지원 grant type 등의 서버 메타데이터를 확인할 수 있어요.

---

## 사전 준비

### 1. 앱 등록이 필요해요

OAuth를 사용하려면 먼저 앱(클라이언트)을 등록해야 해요.
등록하면 `client_id`와 `client_secret`이 발급돼요.

앱 등록은 [DAuth 웹](https://dauth.dodam.b1nd.com)에서 할 수 있어요. 도담도담 계정으로 로그인한 뒤 "새 앱 등록"을 진행하세요.

### 2. Redirect URI를 준비해요

OAuth 인증이 완료되면 유저가 돌아올 URL이 필요해요.
- 운영 환경: **반드시 HTTPS**여야 해요
- 개발 환경: `http://localhost` 는 허용돼요

### 3. 필요한 Scope를 결정해요

앱이 접근할 데이터 범위를 정해요. [Scope 목록](#scope-목록)을 참고하세요.

---

## 앱 등록

[DAuth 웹](https://dauth.dodam.b1nd.com)에서 등록하거나, API로 직접 등록할 수 있어요.

> ⚠️ 앱 등록에는 도담도담 OAuth 로그인이 필요해요. 등록한 유저가 앱의 소유자가 돼요.

### API로 등록

```http
POST /oauth/clients
Authorization: Bearer {OAuth access_token}
Content-Type: application/json

{
  "clientName": "우리 학교 앱",
  "redirectUris": ["https://myapp.com/callback"],
  "scopes": ["profile:read", "outgoing:read"],
  "websiteUrl": "https://myapp.com",
  "description": "학생들을 위한 편의 앱",
  "logoUrl": "https://myapp.com/logo.png"
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| `clientName` | O | 앱 이름 (2~100자) |
| `redirectUris` | O | 콜백 URL 목록 (최소 1개, HTTPS 필수 — localhost 예외) |
| `scopes` | O | 요청할 권한 목록 |
| `websiteUrl` | X | 앱 웹사이트 URL |
| `description` | X | 앱 설명 |
| `logoUrl` | X | 앱 로고 URL (동의 화면에 표시돼요) |

### 응답

```json
{
  "status": 201,
  "message": "Client registered",
  "data": {
    "clientId": "dodam_a1b2c3d4e5f6",
    "clientSecret": "dcs_xxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "clientName": "우리 학교 앱",
    "redirectUris": ["https://myapp.com/callback"],
    "scopes": ["profile:read", "outgoing:read"],
    "createdAt": "2026-03-22T00:00:00"
  }
}
```

> ⚠️ **중요:** `clientSecret`은 이 응답에서만 평문으로 확인할 수 있어요. 반드시 안전한 곳에 저장하세요. 분실 시 재발급만 가능해요.

---

## OAuth 로그인 구현

### 전체 흐름

```
[유저] → [내 서비스] → [DAuth 인증 페이지] → [내 서비스]

1. 내 서비스에서 "도담도담으로 로그인" 버튼 클릭
2. DAuth 인증 페이지로 이동
3. 유저가 도담도담에 로그인하고 권한을 허용 (이전에 허용했으면 자동 스킵)
4. 내 서비스의 callback URL로 code가 전달됨
5. 내 서비스가 code로 access_token을 교환
6. access_token으로 API 호출
```

### Step 1: 인증 요청

유저를 DAuth 인증 페이지로 보내요.

```
https://dauth.dodam.b1nd.com/authorize
  ?client_id={client_id}
  &redirect_uri={redirect_uri}
  &scope={scope}
  &state={state}
  &response_type=code
  &code_challenge={code_challenge}
  &code_challenge_method=S256
```

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `client_id` | O | 앱 등록 시 발급받은 client_id |
| `redirect_uri` | O | 등록된 redirect URI 중 하나 |
| `scope` | O | 요청할 권한 (공백으로 구분, 예: `profile:read outgoing:read`) |
| `state` | 권장 | CSRF 방지용 랜덤 문자열. 그대로 callback에 반환돼요 |
| `response_type` | O | 항상 `code` |
| `code_challenge` | 권장 | PKCE용. `BASE64URL(SHA256(code_verifier))` 값 |
| `code_challenge_method` | 권장 | 항상 `S256` |

> 💡 **PKCE란?** SPA나 모바일 앱처럼 client_secret을 안전하게 보관할 수 없는 환경에서 사용하는 보안 강화 메커니즘이에요. 서버 사이드 앱에서도 사용을 권장해요.

### Step 2: 유저 인증 + 동의

유저가 도담도담에 로그인하고, 앱이 요청한 권한을 확인한 뒤 "허용" 또는 "거부"를 선택해요.

- **처음 방문**: 동의 화면이 표시돼요
- **이전에 허용한 적 있음**: 동의 화면 없이 자동으로 진행돼요
- **관리자가 trusted로 설정한 앱**: 항상 동의 화면 없이 자동 진행돼요

### Step 3: Callback 수신

유저가 허용하면, 등록한 redirect_uri로 리다이렉트돼요.

**허용 시:**
```
https://myapp.com/callback?code=abc123...&state=xyz
```

**거부 시:**
```
https://myapp.com/callback?error=access_denied&state=xyz
```

> ⚠️ 반드시 `state` 값이 Step 1에서 보낸 값과 일치하는지 확인하세요.

### Step 4: 토큰 교환

받은 `code`로 access_token을 교환해요. **이 요청은 반드시 서버 사이드에서 해야 해요** (client_secret 보호).

```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code={받은 code}
&redirect_uri={Step 1과 동일한 redirect_uri}
&client_id={client_id}
&client_secret={client_secret}
&code_verifier={Step 1에서 생성한 code_verifier}
```

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `grant_type` | O | 항상 `authorization_code` |
| `code` | O | callback에서 받은 authorization code |
| `redirect_uri` | O | 인증 요청 시 사용한 것과 동일해야 해요 |
| `client_id` | O | 앱의 client_id |
| `client_secret` | O | 앱의 client_secret |
| `code_verifier` | PKCE 시 | code_challenge 생성에 사용한 원본 문자열 |

**응답:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 180,
  "refresh_token": "drt_xxxxxxxxxxxxxxxxxxxx",
  "scope": "profile:read outgoing:read"
}
```

| 필드 | 설명 |
|------|------|
| `access_token` | API 호출 시 사용하는 JWT 토큰 |
| `token_type` | 항상 `Bearer` |
| `expires_in` | 토큰 만료까지 남은 초 (기본 180초 = 3분) |
| `refresh_token` | 토큰 갱신 시 사용하는 토큰 (30일 유효) |
| `scope` | 실제 부여된 권한 |

> ⚠️ `code`는 **1회만 사용**할 수 있어요. 재사용 시 `invalid_grant` 에러가 발생해요.
> ⚠️ `code`는 **10분** 내에 사용해야 해요.

---

## 토큰으로 API 호출

발급받은 `access_token`을 `Authorization` 헤더에 넣어서 API를 호출해요.

```http
GET /outgoing/my
Authorization: Bearer {access_token}
```

**정상 응답:** 각 API의 응답 형식을 따라요.

**에러 응답:**
- `401 Unauthorized` — 토큰이 만료되었거나 유효하지 않아요
- `403 Forbidden` — 해당 API에 필요한 scope가 없어요

### Scope별 접근 가능한 API

| scope | 접근 가능한 경로 | HTTP 메서드 |
|-------|-----------------|-------------|
| `nightstudy:read` | `/nightstudy/**` | GET |
| `outgoing:read` | `/outgoing/**` | GET |
| `wakeupsong:read` | `/wakeup-song/**` | GET |
| `wakeupsong:write` | `/wakeup-song/**` | POST, PUT, DELETE |
| `profile:read` | `/user/**` | GET |

예를 들어 `outgoing:read` scope만 있는 토큰으로 `/wakeup-song/` API를 호출하면 `403 Forbidden`이 반환돼요.

> 💡 **trusted 앱**: 관리자가 trusted로 설정한 앱의 토큰은 scope 제한 없이 **모든 API에 접근**할 수 있어요.

---

## 토큰 갱신

access_token이 만료되면 refresh_token으로 새 토큰을 발급받을 수 있어요.

```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token={refresh_token}
&client_id={client_id}
&client_secret={client_secret}
```

**응답:** 토큰 교환과 동일한 형식이에요.

> 💡 갱신하면 이전 refresh_token은 자동으로 폐기돼요. 응답의 새 refresh_token을 저장하세요.
> ⚠️ refresh_token 유효기간은 **30일**이에요.

---

## 토큰 폐기

유저가 로그아웃하거나, 토큰이 더 이상 필요 없을 때 폐기할 수 있어요.

```http
POST /oauth/token/revoke
Content-Type: application/x-www-form-urlencoded

token={access_token 또는 refresh_token}
```

**응답:** 항상 `200 OK`를 반환해요. 이미 폐기된 토큰이나 존재하지 않는 토큰을 보내도 200이에요.

---

## Token Exchange (인앱 WebView)

도담도담 공식 앱(React Native)에서 이미 도담도담 access token을 보유하고 있을 때, 유저 인터랙션 없이 OAuth 토큰을 발급받을 수 있어요. **RFC 8693** 표준을 따라요.

### 언제 사용하나요?

- 도담도담 공식 앱에서 WebView를 열 때
- 이미 로그인된 상태에서 OAuth 토큰이 필요할 때
- 유저에게 다시 로그인/동의를 요구하고 싶지 않을 때

### 요청

도담 access token은 `Authorization: Bearer` 헤더로 전달해요. Gateway가 토큰을 검증하고, 유효한 경우에만 OAuth 토큰이 발급돼요.

```http
POST /oauth/token
Authorization: Bearer {도담_access_token}
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:token-exchange
&subject_token_type=urn:ietf:params:oauth:token-type:access_token
&client_id={client_id}
&scope=profile:read outgoing:read
```

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `grant_type` | O | `urn:ietf:params:oauth:grant-type:token-exchange` |
| `subject_token_type` | O | `urn:ietf:params:oauth:token-type:access_token` |
| `client_id` | O | 인앱 전용 client_id |
| `scope` | O | 요청할 권한 |

**응답:** 토큰 교환과 동일한 형식이에요.

> 💡 Token Exchange는 **client_secret이 필요 없어요.** Gateway가 도담 access token의 유효성을 보장해요.
> ⚠️ 이 grant type은 인앱(WebView) 환경에서만 사용하세요.
> ⚠️ 도담 access token을 URL 파라미터나 body에 넣지 마세요. 반드시 Authorization 헤더로 전달해야 해요.

### React Native → WebView 예시

```typescript
// React Native 측
<WebView
  source={{ uri: `https://myapp.com/webview?token=${dodamAccessToken}` }}
/>

// WebView 측 (JavaScript)
const params = new URLSearchParams(window.location.search);
const dodamToken = params.get('token');

const response = await fetch('https://dodam.b1nd.com/oauth/token', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${dodamToken}`,
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  body: new URLSearchParams({
    grant_type: 'urn:ietf:params:oauth:grant-type:token-exchange',
    subject_token_type: 'urn:ietf:params:oauth:token-type:access_token',
    client_id: 'dodam_inapp_xxxxx',
    scope: 'profile:read outgoing:read',
  }),
});

const { access_token } = await response.json();
// 이제 access_token으로 API 호출 가능
```

---

## Scope 목록

| scope | 설명 |
|-------|------|
| `nightstudy:read` | 야간 자율학습 정보를 조회할 수 있어요 |
| `outgoing:read` | 외출/외박 정보를 조회할 수 있어요 |
| `wakeupsong:read` | 기상송 정보를 조회할 수 있어요 |
| `wakeupsong:write` | 기상송을 신청할 수 있어요 |
| `profile:read` | 유저의 기본 프로필(이름, 학번)을 조회할 수 있어요 |

scope는 공백으로 구분해요: `profile:read outgoing:read wakeupsong:read`

전체 scope 목록은 API로도 확인할 수 있어요:
```http
GET /oauth/clients/scopes
```

---

## 앱 관리 API

등록한 앱의 정보를 수정하거나 관리할 수 있어요.

### 앱 조회

```http
GET /oauth/clients/{clientId}
```

clientSecret은 응답에 포함되지 않아요.

### 앱 수정

```http
PUT /oauth/clients/{clientId}
Authorization: Basic {base64(clientId:clientSecret)}
Content-Type: application/json

{
  "clientName": "수정된 앱 이름",
  "redirectUris": ["https://myapp.com/callback"],
  "scopes": ["profile:read", "outgoing:read", "wakeupsong:read"]
}
```

### Secret 재발급

```http
POST /oauth/clients/{clientId}/secret/reset
Authorization: Basic {base64(clientId:clientSecret)}
```

> ⚠️ 재발급하면 이전 secret은 즉시 무효화돼요.

### 앱 비활성화

```http
DELETE /oauth/clients/{clientId}
Authorization: Basic {base64(clientId:clientSecret)}
```

> ⚠️ 비활성화하면 해당 앱으로 새 토큰을 발급받을 수 없어요.

### 소유권 이전

앱의 소유자를 다른 도담도담 유저로 변경할 수 있어요.

```http
POST /oauth/clients/{clientId}/transfer
Authorization: Basic {base64(clientId:clientSecret)}
Content-Type: application/json

{
  "newOwnerPublicId": "새 소유자의 유저 공개 ID"
}
```

---

## 에러 처리

### OAuth 표준 에러 (토큰 관련 API)

`/oauth/token`, `/oauth/token/revoke`, `/oauth/token/introspect` 응답:

```json
{
  "error": "에러코드",
  "error_description": "사람이 읽을 수 있는 설명"
}
```

| error | HTTP | 설명 |
|-------|------|------|
| `invalid_client` | 401 | client_id 또는 client_secret이 잘못되었어요 |
| `invalid_grant` | 400 | authorization code가 만료되었거나 이미 사용되었어요 |
| `invalid_scope` | 400 | 허용되지 않은 scope를 요청했어요 |
| `unsupported_grant_type` | 400 | 지원하지 않는 grant_type이에요 |
| `access_denied` | 400 | 유저가 권한 요청을 거부했어요 |
| `invalid_code_verifier` | 400 | PKCE code_verifier 검증에 실패했어요 |

### 도담 래퍼 에러 (클라이언트 관리 등)

`/oauth/clients`, `/oauth/authorize` 등의 응답:

```json
{
  "status": 400,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### HTTP 상태 코드 에러

| 상태 코드 | 의미 | 대응 방법 |
|-----------|------|-----------|
| 401 | 토큰이 만료되었거나 유효하지 않아요 | refresh_token으로 갱신하세요 |
| 403 | 필요한 scope가 없어요 | 앱 등록 시 scope를 추가하세요 |
| 400 | 요청 파라미터가 잘못되었어요 | error_description을 확인하세요 |

---

## 보안 주의사항

### client_secret 관리
- `client_secret`은 **서버 사이드에서만** 사용하세요
- 프론트엔드 코드, 모바일 앱 번들에 절대 포함하지 마세요
- 환경변수나 시크릿 매니저를 사용하세요
- SPA/모바일 앱은 PKCE를 사용하고, 백엔드 프록시를 통해 토큰 교환을 하세요

### 도담 access token 보호
- 도담 내부 access token은 **모든 API에 접근할 수 있는 강력한 토큰**이에요
- 브라우저 JavaScript에 절대 노출하지 마세요
- Token Exchange 시에만 Authorization 헤더로 전달하고, URL 파라미터나 body에 넣지 마세요
- 인앱 WebView에서만 사용하세요

### OAuth 토큰 vs 도담 토큰
| | 도담 access token | OAuth access token |
|---|---|---|
| 접근 범위 | 모든 API (무제한) | 허용된 scope만 |
| 유효기간 | 5분 | 3분 |
| 탈취 시 피해 | 전체 계정 접근 가능 | scope 범위 내만 접근 가능 |
| 브라우저 노출 | 절대 금지 | SPA에서 사용 가능 |

### PKCE 사용
- SPA, 모바일 앱에서는 **반드시 PKCE를 사용**하세요
- `code_verifier`는 요청마다 새로 생성하세요
- `S256` 방식만 지원해요

### State 파라미터
- CSRF 공격 방지를 위해 `state` 파라미터를 **반드시 사용**하세요
- 랜덤한 문자열을 생성하고, callback에서 일치 여부를 검증하세요

### Redirect URI
- 등록된 redirect_uri와 정확히 일치해야 해요
- 와일드카드는 지원하지 않아요
- 운영 환경에서는 HTTPS만 허용돼요

### 토큰 저장
- access_token은 메모리 또는 sessionStorage에 저장하세요
- refresh_token은 httpOnly cookie 또는 서버 세션에 저장하는 것을 권장해요
- localStorage에 토큰을 저장하면 XSS 공격에 취약할 수 있어요

---

## 언어별 예제

### Node.js (Express)

```javascript
const express = require('express');
const axios = require('axios');
const crypto = require('crypto');

const app = express();

const CLIENT_ID = process.env.OAUTH_CLIENT_ID;
const CLIENT_SECRET = process.env.OAUTH_CLIENT_SECRET;
const REDIRECT_URI = 'https://myapp.com/callback';
const DODAM_URL = 'https://dodam.b1nd.com';
const DAUTH_URL = 'https://dauth.dodam.b1nd.com';

// Step 1: 로그인 시작
app.get('/login', (req, res) => {
  const state = crypto.randomBytes(16).toString('hex');
  req.session.oauthState = state;

  const url = `${DAUTH_URL}/authorize?` + new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    scope: 'profile:read outgoing:read',
    state: state,
    response_type: 'code',
  });

  res.redirect(url);
});

// Step 2: Callback
app.get('/callback', async (req, res) => {
  const { code, state, error } = req.query;

  if (error) return res.send(`에러: ${error}`);
  if (state !== req.session.oauthState) return res.status(400).send('state 불일치');

  // Step 3: 토큰 교환
  const response = await axios.post(`${DODAM_URL}/oauth/token`,
    new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      redirect_uri: REDIRECT_URI,
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
    }),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  );

  const { access_token, refresh_token } = response.data;
  req.session.accessToken = access_token;
  req.session.refreshToken = refresh_token;

  res.redirect('/dashboard');
});

// Step 4: API 호출
app.get('/outgoing', async (req, res) => {
  const response = await axios.get(`${DODAM_URL}/outgoing/my`, {
    headers: { Authorization: `Bearer ${req.session.accessToken}` },
  });
  res.json(response.data);
});
```

### Python (Flask + authlib)

```python
import os
from flask import Flask, redirect, session, request, jsonify
from authlib.integrations.flask_client import OAuth

app = Flask(__name__)
app.secret_key = os.urandom(24)

oauth = OAuth(app)
dodam = oauth.register(
    name='dodam',
    client_id=os.environ['OAUTH_CLIENT_ID'],
    client_secret=os.environ['OAUTH_CLIENT_SECRET'],
    server_metadata_url='https://dodam.b1nd.com/.well-known/openid-configuration',
    client_kwargs={'scope': 'profile:read outgoing:read'},
)

@app.route('/login')
def login():
    return dodam.authorize_redirect('https://myapp.com/callback')

@app.route('/callback')
def callback():
    token = dodam.authorize_access_token()
    session['token'] = token
    return redirect('/dashboard')

@app.route('/outgoing')
def outgoing():
    resp = dodam.get('/outgoing/my', token=session['token'])
    return jsonify(resp.json())
```

> 💡 Python authlib는 `/.well-known/openid-configuration`을 자동으로 읽어서 엔드포인트를 설정해요. 별도 URL 설정이 필요 없어요.

### Go (golang.org/x/oauth2)

```go
package main

import (
    "golang.org/x/oauth2"
    "net/http"
    "os"
)

var oauthConfig = &oauth2.Config{
    ClientID:     os.Getenv("OAUTH_CLIENT_ID"),
    ClientSecret: os.Getenv("OAUTH_CLIENT_SECRET"),
    RedirectURL:  "https://myapp.com/callback",
    Scopes:       []string{"profile:read", "outgoing:read"},
    Endpoint: oauth2.Endpoint{
        AuthURL:  "https://dauth.dodam.b1nd.com/authorize",
        TokenURL: "https://dodam.b1nd.com/oauth/token",
    },
}

func loginHandler(w http.ResponseWriter, r *http.Request) {
    state := generateRandomState()
    url := oauthConfig.AuthCodeURL(state)
    http.Redirect(w, r, url, http.StatusTemporaryRedirect)
}

func callbackHandler(w http.ResponseWriter, r *http.Request) {
    code := r.URL.Query().Get("code")
    token, err := oauthConfig.Exchange(r.Context(), code)
    if err != nil {
        http.Error(w, err.Error(), http.StatusBadRequest)
        return
    }
    // token.AccessToken 으로 API 호출
}
```

### Kotlin (Spring Boot)

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          dodam:
            client-id: ${OAUTH_CLIENT_ID}
            client-secret: ${OAUTH_CLIENT_SECRET}
            scope: profile:read,outgoing:read
            redirect-uri: "{baseUrl}/login/oauth2/code/dodam"
            authorization-grant-type: authorization_code
        provider:
          dodam:
            issuer-uri: https://dodam.b1nd.com
```

Spring Security OAuth2 Client가 `/.well-known/openid-configuration`을 자동으로 읽어서 모든 설정을 해줘요.

---

## PKCE 구현 가이드

### code_verifier 생성

43~128자의 랜덤 문자열을 생성해요. URL-safe 문자만 사용해요.

```javascript
function generateCodeVerifier() {
  const array = new Uint8Array(64);
  crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...array))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}
```

### code_challenge 생성

code_verifier를 SHA-256으로 해시하고 Base64URL로 인코딩해요.

```javascript
async function generateCodeChallenge(verifier) {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}
```

### 사용 순서

1. `code_verifier`를 생성하고 안전한 곳에 저장해요 (세션, 메모리 등)
2. `code_challenge`를 계산해요
3. authorize 요청에 `code_challenge`와 `code_challenge_method=S256`을 포함해요
4. 토큰 교환 요청에 원본 `code_verifier`를 포함해요
5. 서버가 `SHA256(code_verifier) == code_challenge`를 검증해요

---

## FAQ

### Q: access_token 유효기간이 얼마나 되나요?
**A:** 기본 **3분(180초)**이에요. 만료되면 refresh_token으로 갱신하세요.

### Q: refresh_token 유효기간은요?
**A:** **30일**이에요. 30일이 지나면 유저가 다시 로그인해야 해요.

### Q: 한번 동의하면 다음부터는 안 뜨나요?
**A:** 네, 같은 scope로 이전에 동의했으면 다음부터는 자동으로 진행돼요. scope가 변경되면 다시 동의가 필요해요.

### Q: trusted 앱이란 뭔가요?
**A:** 관리자가 신뢰할 수 있다고 설정한 앱이에요. trusted 앱은 동의 화면 없이 자동 진행되고, scope 제한 없이 모든 API에 접근할 수 있어요. (예: DAuth 웹, 도담도담 공식 앱)

### Q: 유저가 허용한 scope를 나중에 변경할 수 있나요?
**A:** 현재는 전체 수락/거부만 가능해요. scope를 변경하려면 유저가 다시 동의해야 해요.

### Q: 여러 redirect_uri를 등록할 수 있나요?
**A:** 네, 앱 등록 시 여러 개를 등록할 수 있어요. authorize 요청 시 등록된 URI 중 하나를 사용하면 돼요.

### Q: 표준 OAuth 라이브러리를 그대로 사용할 수 있나요?
**A:** 네, `/.well-known/openid-configuration`을 지원하므로 대부분의 표준 라이브러리(passport-oauth2, authlib, golang.org/x/oauth2 등)가 자동으로 동작해요.

### Q: client_secret을 분실했어요.
**A:** Secret 재발급 API를 사용하세요. 이전 secret으로 인증한 뒤 새 secret을 발급받을 수 있어요. 이전 secret도 분실했다면 관리자에게 문의하세요.

### Q: access_token에 어떤 정보가 들어있나요?
**A:** JWT payload에는 `sub`(유저 공개 ID), `scope`(권한), `aud`(client_id), `exp`(만료시간)만 포함돼요. 이름, 이메일 등 개인정보는 포함되지 않아요.

### Q: 앱 소유권을 다른 사람에게 넘길 수 있나요?
**A:** 네, 소유권 이전 API로 가능해요. 현재 client_secret으로 인증한 뒤 새 소유자의 유저 공개 ID를 지정하면 돼요.
