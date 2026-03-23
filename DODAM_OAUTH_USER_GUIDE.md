# DAuth 사용자 가이드

도담도담 OAuth(DAuth)를 이용해서 여러분의 서비스에 "도담도담으로 로그인" 기능을 추가하는 방법을 안내해요.

---

## 목차

1. [DAuth가 뭔가요?](#dauth가-뭔가요)
2. [시작하기 — 앱 등록](#시작하기--앱-등록)
3. [전체 흐름 한눈에 보기](#전체-흐름-한눈에-보기)
4. [프론트엔드 구현 (React SPA)](#프론트엔드-구현-react-spa)
5. [백엔드 구현 (Spring Boot)](#백엔드-구현-spring-boot)
6. [토큰으로 API 호출하기](#토큰으로-api-호출하기)
7. [토큰 갱신하기](#토큰-갱신하기)
8. [사용 가능한 권한 (Scope)](#사용-가능한-권한-scope)
9. [에러 처리](#에러-처리)
10. [보안 주의사항](#보안-주의사항)
11. [다른 언어 예제](#다른-언어-예제)
12. [자주 묻는 질문](#자주-묻는-질문)

---

## DAuth가 뭔가요?

DAuth는 도담도담의 OAuth 2.0 인증 서비스예요. **"구글로 로그인"** 버튼처럼 **"도담도담으로 로그인"** 버튼을 만들 수 있어요.

유저의 비밀번호는 절대 여러분의 서비스에 전달되지 않아요. 유저가 허용한 범위(scope) 내에서만 API를 호출할 수 있어요.

---

## 시작하기 — 앱 등록

### 1단계: DAuth 웹에서 앱 등록

[DAuth 웹사이트](https://dauth.dodam.b1nd.com)에 도담도담 계정으로 로그인하고, "새 앱 등록"을 클릭해요.

| 입력 항목 | 설명 | 예시 |
|-----------|------|------|
| 앱 이름 | 여러분의 서비스 이름 | 우리반 시간표 |
| 설명 | 앱이 하는 일 | 학생들의 시간표를 보여주는 웹 앱 |
| 웹사이트 URL | 서비스 주소 | https://timetable.example.com |
| Redirect URI | 로그인 완료 후 돌아올 주소 | https://timetable.example.com/auth/callback |
| 권한 (Scope) | 필요한 데이터 범위 | profile:read |

### 2단계: Client ID, Secret 저장

등록하면 두 가지 값을 받아요:

- **Client ID** (`dodam_xxxxxxxxxxxx`) — 앱을 식별하는 공개 ID
- **Client Secret** (`dcs_xxxxxxxxxxxx`) — 앱의 비밀 키

> ⚠️ **Client Secret은 이 화면에서 딱 한 번만 볼 수 있어요!** 반드시 안전한 곳에 복사해서 저장하세요.

### 3단계: 환경변수 설정

**Spring Boot (application.yml):**
```yaml
dauth:
  client-id: dodam_xxxxxxxxxxxx
  client-secret: dcs_xxxxxxxxxxxx
  redirect-uri: https://myapp.com/auth/callback
```

> ⚠️ **Client Secret은 서버에만 저장하세요.** 프론트엔드 코드에 절대 넣지 마세요!

---

## 전체 흐름 한눈에 보기

```
[유저]                    [React SPA]                    [Spring Boot 서버]              [DAuth]
  │                           │                              │                            │
  │  1. "도담 로그인" 클릭     │                              │                            │
  │──────────────────────────>│                              │                            │
  │                           │                              │                            │
  │  2. DAuth 로그인 페이지로 redirect                       │                            │
  │<──────────────────────────│                              │                            │
  │                           │                              │                            │
  │  3. 도담도담 로그인 + 권한 허용                           │                            │
  │─────────────────────────────────────────────────────────────────────────────────────>│
  │                           │                              │                            │
  │  4. /auth/callback?code=abc123 으로 돌아옴               │                            │
  │──────────────────────────>│                              │                            │
  │                           │                              │                            │
  │                           │  5. code를 서버로 전달        │                            │
  │                           │─────────────────────────────>│                            │
  │                           │                              │                            │
  │                           │                              │  6. code → access_token     │
  │                           │                              │──────────────────────────>│
  │                           │                              │                            │
  │                           │                              │  7. access_token 반환       │
  │                           │                              │<──────────────────────────│
  │                           │                              │                            │
  │                           │  8. 로그인 완료 (세션/쿠키)   │                            │
  │                           │<─────────────────────────────│                            │
  │  9. 로그인 완료!           │                              │                            │
  │<──────────────────────────│                              │                            │
```

**핵심**: 프론트엔드는 **redirect만** 하고, 토큰 교환은 **반드시 서버에서** 해요.

---

## 프론트엔드 구현 (React SPA)

프론트엔드에서 할 일은 딱 두 가지예요:
1. **로그인 버튼** → DAuth 로그인 페이지로 redirect
2. **Callback 페이지** → URL에서 code를 받아서 서버로 전달

### 1. 로그인 버튼

```tsx
// src/components/LoginButton.tsx

const DAUTH_CLIENT_ID = "dodam_xxxxxxxxxxxx"; // 공개 값이라 프론트에 있어도 돼요
const REDIRECT_URI = "https://myapp.com/auth/callback";

export default function LoginButton() {
  const handleLogin = () => {
    // CSRF 방지용 랜덤 문자열
    const state = crypto.randomUUID();
    sessionStorage.setItem("oauth_state", state);

    const params = new URLSearchParams({
      client_id: DAUTH_CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      scope: "profile:read",        // 필요한 권한
      state: state,                  // CSRF 방지
      response_type: "code",         // 항상 "code"
    });

    // DAuth 로그인 페이지로 이동
    window.location.href = `https://dauth.dodam.b1nd.com/authorize?${params}`;
  };

  return (
    <button onClick={handleLogin}>
      도담도담으로 로그인
    </button>
  );
}
```

**파라미터 설명:**

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `client_id` | O | 앱 등록 시 받은 Client ID (공개 값) |
| `redirect_uri` | O | 로그인 완료 후 돌아올 URL. 앱 등록 시 입력한 것과 **정확히 일치**해야 해요 |
| `scope` | O | 필요한 권한. 여러 개는 공백으로 구분 (예: `profile:read outgoing:read`) |
| `state` | 권장 | CSRF 공격 방지용 랜덤 문자열 |
| `response_type` | O | 항상 `code` |

### 2. Callback 페이지

유저가 로그인하고 권한을 허용하면, DAuth가 여러분의 `redirect_uri`로 보내줘요:

**허용 시:** `https://myapp.com/auth/callback?code=aBcDeFg...&state=아까보낸값`

**거부 시:** `https://myapp.com/auth/callback?error=access_denied&state=아까보낸값`

```tsx
// src/pages/AuthCallback.tsx
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";

export default function AuthCallback() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const code = searchParams.get("code");
    const state = searchParams.get("state");
    const error = searchParams.get("error");

    // 1. 에러 확인
    if (error) {
      alert("로그인이 거부되었어요.");
      navigate("/");
      return;
    }

    // 2. state 검증 (CSRF 방지)
    const savedState = sessionStorage.getItem("oauth_state");
    if (!code || state !== savedState) {
      alert("잘못된 요청이에요.");
      navigate("/");
      return;
    }
    sessionStorage.removeItem("oauth_state");

    // 3. code를 내 서버로 전달 (서버가 토큰 교환)
    axios.post("/api/auth/callback", { code })
      .then(() => navigate("/"))
      .catch(() => alert("로그인에 실패했어요."));
  }, [searchParams, navigate]);

  return <p>로그인 처리 중...</p>;
}
```

### 3. 라우터 설정

```tsx
// src/App.tsx
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import AuthCallback from "./pages/AuthCallback";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
      </Routes>
    </BrowserRouter>
  );
}
```

### 4. API 호출 (서버 경유)

프론트엔드에서 도담 API를 직접 호출하지 마세요. 서버를 경유해야 해요:

```tsx
// src/api/user.ts
import axios from "axios";

// 내 서버를 경유해서 도담 API 호출
export async function getMyProfile() {
  const { data } = await axios.get("/api/user/me");
  return data;
}
```

> ⚠️ **`https://dodam.b1nd.com/user/me`를 프론트에서 직접 호출하지 마세요!** access_token이 브라우저에 노출돼요.

---

## 백엔드 구현 (Spring Boot)

### 프로젝트 설정

**build.gradle.kts:**
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis") // 세션 저장용 (선택)
}
```

**application.yml:**
```yaml
dauth:
  client-id: ${DAUTH_CLIENT_ID}
  client-secret: ${DAUTH_CLIENT_SECRET}
  redirect-uri: ${DAUTH_REDIRECT_URI}
  token-url: https://dodam.b1nd.com/oauth/token
  api-url: https://dodam.b1nd.com
```

### 1. 설정 클래스

```kotlin
@ConfigurationProperties(prefix = "dauth")
data class DAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val tokenUrl: String,
    val apiUrl: String,
)
```

### 2. 토큰 교환 서비스

```kotlin
@Service
class DAuthService(
    private val properties: DAuthProperties,
    private val restTemplate: RestTemplate,
) {
    /**
     * authorization code를 access_token으로 교환해요.
     */
    fun exchangeToken(code: String): DAuthTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("redirect_uri", properties.redirectUri)
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val request = HttpEntity(params, headers)
        val response = restTemplate.postForEntity(
            properties.tokenUrl,
            request,
            DAuthTokenResponse::class.java
        )

        return response.body ?: throw RuntimeException("토큰 교환 실패")
    }

    /**
     * refresh_token으로 새 access_token을 발급받아요.
     */
    fun refreshToken(refreshToken: String): DAuthTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken)
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val request = HttpEntity(params, headers)
        val response = restTemplate.postForEntity(
            properties.tokenUrl,
            request,
            DAuthTokenResponse::class.java
        )

        return response.body ?: throw RuntimeException("토큰 갱신 실패")
    }

    /**
     * access_token으로 도담 API를 호출해요.
     */
    fun <T> callApi(path: String, accessToken: String, responseType: Class<T>): T {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        val request = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            "${properties.apiUrl}$path",
            HttpMethod.GET,
            request,
            responseType
        )

        return response.body ?: throw RuntimeException("API 호출 실패")
    }
}
```

### 3. 응답 DTO

```kotlin
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DAuthTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String,
    val scope: String,
)

data class DodamApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?,
)

data class DodamUserInfo(
    val publicId: String,
    val username: String,
    val name: String,
    val phone: String?,
    val profileImage: String?,
    val status: String,
    val roles: List<String>,
    val student: StudentInfo?,
    val createdAt: String,
)

data class StudentInfo(
    val grade: Int,
    val room: Int,
    val number: Int,
)
```

### 4. 컨트롤러

```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val dAuthService: DAuthService,
    private val httpSession: HttpSession,
) {
    /**
     * 프론트에서 code를 받아서 토큰 교환
     */
    @PostMapping("/callback")
    fun callback(@RequestBody request: CallbackRequest): ResponseEntity<Map<String, Boolean>> {
        val tokens = dAuthService.exchangeToken(request.code)

        // 세션에 토큰 저장
        httpSession.setAttribute("access_token", tokens.accessToken)
        httpSession.setAttribute("refresh_token", tokens.refreshToken)

        return ResponseEntity.ok(mapOf("success" to true))
    }

    /**
     * 로그인 상태 확인
     */
    @GetMapping("/check")
    fun check(): ResponseEntity<Map<String, Boolean>> {
        val loggedIn = httpSession.getAttribute("access_token") != null
        return ResponseEntity.ok(mapOf("loggedIn" to loggedIn))
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, Boolean>> {
        httpSession.invalidate()
        return ResponseEntity.ok(mapOf("success" to true))
    }

    data class CallbackRequest(val code: String)
}
```

### 5. API 프록시 (도담 API를 서버 경유로 호출)

```kotlin
@RestController
@RequestMapping("/api/user")
class UserController(
    private val dAuthService: DAuthService,
    private val httpSession: HttpSession,
) {
    @GetMapping("/me")
    fun getMe(): ResponseEntity<Any> {
        val accessToken = httpSession.getAttribute("access_token") as? String
            ?: return ResponseEntity.status(401).body(mapOf("error" to "로그인 필요"))

        return try {
            val typeRef = object : ParameterizedTypeReference<DodamApiResponse<DodamUserInfo>>() {}
            val response = dAuthService.callApi("/user/me", accessToken, typeRef)
            ResponseEntity.ok(response)
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                // 토큰 만료 → 갱신 시도
                val refreshToken = httpSession.getAttribute("refresh_token") as? String
                    ?: return ResponseEntity.status(401).body(mapOf("error" to "세션 만료"))

                return try {
                    val newTokens = dAuthService.refreshToken(refreshToken)
                    httpSession.setAttribute("access_token", newTokens.accessToken)
                    httpSession.setAttribute("refresh_token", newTokens.refreshToken)

                    // 재시도
                    val response = dAuthService.callApi("/user/me", newTokens.accessToken, typeRef)
                    ResponseEntity.ok(response)
                } catch (e2: Exception) {
                    httpSession.invalidate()
                    ResponseEntity.status(401).body(mapOf("error" to "세션 만료. 다시 로그인하세요."))
                }
            }
            ResponseEntity.status(e.statusCode).body(mapOf("error" to "API 호출 실패"))
        }
    }
}
```

### 6. RestTemplate 빈 등록

```kotlin
@Configuration
class WebConfig {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder().build()
}
```

---

## 토큰으로 API 호출하기

서버에서 `access_token`을 `Authorization` 헤더에 넣어서 도담 API를 호출해요.

```http
GET https://dodam.b1nd.com/user/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

**응답 예시:**

```json
{
  "status": 200,
  "message": "내 정보를 조회했어요.",
  "data": {
    "publicId": "56cfc398-...",
    "username": "kmg",
    "name": "김민규",
    "phone": "01012345678",
    "profileImage": null,
    "status": "ACTIVE",
    "roles": ["STUDENT"],
    "student": { "grade": 2, "room": 2, "number": 7 },
    "createdAt": "2026-02-26T14:55:39"
  }
}
```

> ⚠️ **API 호출은 반드시 서버에서 하세요!** 프론트에서 직접 호출하면 access_token이 노출돼요.

---

## 토큰 갱신하기

access_token 내부의 인증이 만료되면 API 호출 시 `401 Unauthorized`가 돼요. `refresh_token`으로 새 토큰을 발급받으세요.

```http
POST https://dodam.b1nd.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token={저장해둔 refresh_token}
&client_id={Client ID}
&client_secret={Client Secret}
```

**응답:** 토큰 교환과 동일한 형식이에요. 새 `access_token`과 `refresh_token`이 발급돼요.

> 💡 갱신하면 이전 refresh_token은 폐기돼요. **새 refresh_token을 반드시 저장하세요.**
> 💡 유저가 30일 이내에 한 번이라도 접속하면 세션은 계속 유지돼요.

---

## 사용 가능한 권한 (Scope)

| scope | 설명 | 접근 가능한 API |
|-------|------|-----------------|
| `profile:read` | 유저 프로필 조회 | `GET /user/me` |
| `nightstudy:read` | 야자 정보 조회 | `GET /nightstudy/**` |
| `outgoing:read` | 외출/외박 정보 조회 | `GET /outgoing/**` |
| `wakeupsong:read` | 기상송 정보 조회 | `GET /wakeup-song/**` |
| `wakeupsong:write` | 기상송 신청 | `POST /wakeup-song/**` |

여러 scope: `scope=profile:read outgoing:read wakeupsong:read`

---

## 에러 처리

### 토큰 교환 에러

```json
{ "error": "invalid_grant", "error_description": "Authorization code has expired" }
```

| error | HTTP | 의미 |
|-------|------|------|
| `invalid_client` | 401 | Client ID 또는 Secret이 틀림 |
| `invalid_grant` | 400 | code 만료 또는 이미 사용됨 |
| `invalid_scope` | 400 | 등록하지 않은 scope 요청 |
| `access_denied` | 400 | 유저가 권한 거부 |

### API 호출 에러

| HTTP | 의미 | 대응 |
|------|------|------|
| 401 | 토큰 만료 | refresh_token으로 갱신 |
| 403 | scope 부족 | 해당 scope를 앱에 추가 |

---

## 보안 주의사항

### 절대 하면 안 되는 것

- ❌ Client Secret을 프론트엔드 코드에 넣기
- ❌ Client Secret을 GitHub에 올리기
- ❌ 프론트엔드에서 `/oauth/token` 직접 호출하기
- ❌ access_token을 프론트엔드 localStorage에 저장하기

### 권장하는 것

- ✅ Client Secret은 서버 환경변수에만 저장
- ✅ 토큰 교환은 서버에서만 수행
- ✅ access_token은 서버 세션 또는 httpOnly 쿠키에 저장
- ✅ state 파라미터로 CSRF 방지
- ✅ 도담 API 호출은 서버를 경유

### 왜 서버를 거쳐야 하나요?

```
[안전한 방법]
브라우저 → 내 서버 → 도담 API
           ↑ Client Secret과 access_token은 여기에만

[위험한 방법]
브라우저 → 도담 API
↑ Client Secret과 access_token이 브라우저에 노출!
```

---

## 다른 언어 예제

### Node.js (Express)

```javascript
const express = require("express");
const session = require("express-session");
const axios = require("axios");

const app = express();
app.use(express.json());
app.use(session({ secret: "your-secret", resave: false, saveUninitialized: false }));

const CLIENT_ID = process.env.DAUTH_CLIENT_ID;
const CLIENT_SECRET = process.env.DAUTH_CLIENT_SECRET;
const REDIRECT_URI = "https://myapp.com/auth/callback";

app.post("/api/auth/callback", async (req, res) => {
  try {
    const tokenRes = await axios.post("https://dodam.b1nd.com/oauth/token",
      new URLSearchParams({
        grant_type: "authorization_code",
        code: req.body.code,
        redirect_uri: REDIRECT_URI,
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
      }).toString(),
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
    );
    req.session.accessToken = tokenRes.data.access_token;
    req.session.refreshToken = tokenRes.data.refresh_token;
    res.json({ success: true });
  } catch (err) {
    res.status(400).json({ error: err.response?.data?.error_description || "실패" });
  }
});

app.get("/api/user/me", async (req, res) => {
  try {
    const r = await axios.get("https://dodam.b1nd.com/user/me", {
      headers: { Authorization: `Bearer ${req.session.accessToken}` },
    });
    res.json(r.data);
  } catch (err) {
    if (err.response?.status === 401) {
      // 토큰 갱신 후 재시도 (위의 Spring Boot 예제 참고)
    }
    res.status(err.response?.status || 500).json({ error: "실패" });
  }
});

app.listen(3000);
```

### Python (Flask)

```python
import os, secrets, requests
from flask import Flask, redirect, session, request, jsonify

app = Flask(__name__)
app.secret_key = os.urandom(24)

CLIENT_ID = os.environ["DAUTH_CLIENT_ID"]
CLIENT_SECRET = os.environ["DAUTH_CLIENT_SECRET"]
REDIRECT_URI = "https://myapp.com/auth/callback"

@app.route("/api/auth/callback", methods=["POST"])
def auth_callback():
    res = requests.post("https://dodam.b1nd.com/oauth/token", data={
        "grant_type": "authorization_code",
        "code": request.json["code"],
        "redirect_uri": REDIRECT_URI,
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET,
    })
    if res.status_code != 200:
        return jsonify(error=res.json().get("error_description")), 400
    tokens = res.json()
    session["access_token"] = tokens["access_token"]
    session["refresh_token"] = tokens["refresh_token"]
    return jsonify(success=True)

@app.route("/api/user/me")
def me():
    r = requests.get("https://dodam.b1nd.com/user/me",
        headers={"Authorization": f"Bearer {session.get('access_token')}"})
    return jsonify(r.json()), r.status_code
```

### Spring Security OAuth2 Client (자동 설정)

코드 없이 `application.yml`만으로도 동작해요:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          dodam:
            client-id: ${DAUTH_CLIENT_ID}
            client-secret: ${DAUTH_CLIENT_SECRET}
            scope: profile:read
            redirect-uri: "{baseUrl}/login/oauth2/code/dodam"
            authorization-grant-type: authorization_code
        provider:
          dodam:
            issuer-uri: https://dodam.b1nd.com
```

Spring이 `/.well-known/openid-configuration`을 자동으로 읽어서 모든 설정을 해줘요.

---

## 자주 묻는 질문

### Q: code는 한 번만 사용할 수 있나요?
**A:** 네. 1회용이에요. 10분 내에 사용해야 해요.

### Q: 한번 동의하면 다음부터는 안 뜨나요?
**A:** 네, 같은 scope라면 자동으로 넘어가요.

### Q: 유저 비밀번호가 내 서비스에 전달되나요?
**A:** 아니에요! 비밀번호는 도담도담에서만 처리돼요.

### Q: 토큰이 만료되면?
**A:** `refresh_token`으로 갱신하세요. 30일 이내에 한 번이라도 접속하면 세션은 계속 유지돼요.

### Q: Client Secret을 잃어버렸어요.
**A:** DAuth 웹에서 재발급하세요. 이전 Secret도 모르면 관리자(mdev_team@dgsw.hs.kr)에게 문의하세요.

### Q: 표준 OAuth 라이브러리를 쓸 수 있나요?
**A:** 네. `/.well-known/openid-configuration`을 지원하므로 대부분의 표준 라이브러리가 자동으로 동작해요.
