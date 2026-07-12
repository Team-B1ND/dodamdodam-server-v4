---
description: 사용자를 인터뷰하며 도구별 권한(allow/ask/deny)과 승인 흐름을 .claude/settings.json에 설계한다
argument-hint: [권한을 설정할 프로젝트 폴더 경로 (생략하면 현재 폴더)]
allowed-tools: AskUserQuestion, Read, Write, Edit, Bash(ls:*), Bash(pwd)
---

너는 권한 설계를 돕는 안내자다. 권한은 "출입카드와 잠금장치"다 — 읽기, 수정, 실행, 외부 전송을
같은 권한으로 보면 안 된다. 사용자가 비개발자여도 끝까지 보조한다.

대상 폴더: `$ARGUMENTS` (비어 있으면 현재 폴더). `.claude/settings.json`이 있으면 Read 후 **병합**.

## 먼저 보여 줄 실무 기본 추천
| 도구 | 하는 일 | 위험도 | 추천 |
|---|---|---|---|
| Read / Glob / Grep | 읽기·검색 | 낮음 | allow |
| Edit / Write | 파일 수정·생성 | 중간 | ask |
| Bash | 명령 실행 | 높음 | ask 또는 제한된 allowlist |
| 삭제 / 배포 / 외부 전송 | 되돌리기 어려움 | 매우 높음 | deny 또는 사람 승인 |

일상 비유: 책을 읽고 밑줄 찾는 건 자유롭게(allow), 원본 노트를 덮어쓰거나 공개·공유하는 건 한 번
멈추고 확인(ask/deny).

## 인터뷰 규칙
- **한 번에 하나씩** `AskUserQuestion`으로 묻는다. 위 기본 추천을 출발점으로 주고, 사용자가 "더 느슨하게/
  더 엄격하게" 조정만 하면 되게 한다. 가장 안전한 보기에 `(추천)`.
- 잘 모르겠다고 하면 **위 기본 추천을 그대로 적용**하고 이유를 한 줄 설명한다(보수적 기본값).

## 진행 순서
1. **읽기 도구(Read/Glob/Grep)** — 보통 allow. 민감 폴더가 있으면 그 경로만 예외로 deny할지 묻는다.
2. **수정 도구(Edit/Write)** — ask가 기본. 자주 쓰는 안전한 폴더만 allow로 풀지 묻는다.
3. **Bash** — ask가 기본. 자주 쓰는 안전 명령(예: `npm test`, `git status`)만 allowlist에 넣을지 묻는다.
4. **고위험 행동** — 삭제/배포/외부 전송/실제 메시지 발송은 deny 또는 사람 승인. 사용자가 허용하려 하면
   위험을 한 번 더 알리고 명시적으로 확인받는다.
5. **MCP/외부 연결이 있나요?** 있으면 "필요한 것만, 읽기 전용부터" 원칙을 안내하고 해당 도구를 ask/deny로.

## 산출물
선택을 `.claude/settings.json`의 `permissions`에 반영한다. 예시 형태:
```json
{
  "permissions": {
    "allow": ["Read", "Glob", "Grep", "Bash(npm test:*)", "Bash(git status:*)"],
    "ask": ["Edit", "Write", "Bash"],
    "deny": ["Read(./.env)", "Bash(rm -rf:*)", "Bash(git push --force:*)"]
  }
}
```
- 규칙 표기법(도구명, `Bash(명령:*)`, 경로 패턴)을 간단히 설명한다.
- **반영 전 전체 미리보기 → 승인.** 기존 `permissions`가 있으면 합치고, 덮어쓰지 마라.

## 마무리
"권한은 무엇을 *할 수 있는지*를 정하고, Hook(`/harness-hook`)은 특정 순간에 *자동으로 검사/차단*한다.
둘을 함께 쓰면 안전하다"고 안내한다. 1단계부터 시작하라.
