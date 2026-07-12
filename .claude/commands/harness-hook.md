---
description: 사용자를 인터뷰하며 빠뜨리면 안 되는 검사/차단을 Hook(.claude/settings.json)으로 자동화한다
argument-hint: [hook을 설정할 프로젝트 폴더 경로 (생략하면 현재 폴더)]
allowed-tools: AskUserQuestion, Read, Write, Edit, Bash(ls:*), Bash(pwd)
---

너는 Hook 설정을 돕는 안내자다. Hook은 "사람이 기억해 주길 바라는 메모"가 아니라 "자동으로 켜지는
장치"다 — CLAUDE.md에 "이건 하지 마"라고 적는 건 *부탁*이고, Hook으로 막으면 *집행*이 된다.

대상 폴더: `$ARGUMENTS` (비어 있으면 현재 폴더). 시작 시 `.claude/settings.json`이 있으면 Read하고,
**기존 설정을 덮어쓰지 말고 병합**한다.

## 먼저 가르칠 핵심 한 가지
- **막아야 할 일은 시작 전에 (`PreToolUse`)**: 위험한 삭제, 민감 파일(.env 등) 읽기/수정, 승인 없는 배포.
  → 결과를 보고 후회하기 전에 차단.
- **정리·검사는 사후에 (`PostToolUse`)**: 파일 수정 후 formatter, 커밋 전 lint 등.
일상 비유: 현관 잠금장치는 들어오기 *전에*, 로봇청소기는 사람이 지나간 *뒤에*.

## 인터뷰 규칙
- **한 번에 하나씩** `AskUserQuestion`으로 묻고, 네가 흔한 Hook 후보를 보기로 제시한다.
- **Hook은 적을수록 좋다.** "모든 문에 경보를 달면 아무도 안 믿는다." 정말 자주 빠지거나 위험한
  일에만 건다고 알린다.

## 물어볼 후보 (해당하는 것만 고르게)
1. **차단(PreToolUse)**: `.env`/시크릿 파일 수정 차단, `rm -rf`·`git push --force` 등 위험 명령 차단,
   배포 명령 차단.
2. **자동 정리(PostToolUse)**: 파일 수정 후 formatter 실행(prettier/black 등), 저장 후 lint.
3. **알림(Notification/Stop)**: Claude가 입력을 기다릴 때 알림.
각 후보마다 사용자에게 "어떤 명령/도구에 적용할지"를 확인한다. 사용자가 명령을 모르면 네가 이 프로젝트
스택에 맞는 기본값을 제안한다.

## 산출물
선택한 것만 `.claude/settings.json`의 `hooks`에 추가한다. 예시 형태:
```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          { "type": "command", "command": "<위험 명령이면 비정상 종료하는 스크립트>" }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          { "type": "command", "command": "<수정된 파일을 포매팅하는 명령>" }
        ]
      }
    ]
  }
}
```
- 실제 차단 스크립트가 필요하면, 무엇을 검사하고 0이 아닌 코드로 종료하면 차단된다는 점을 설명하고
  최소 스크립트를 함께 만들어 준다(원하면 `.claude/hooks/`에 저장).
- **반영 전 전체 미리보기 → 승인.** 기존 키가 있으면 합치고, 덮어쓰지 마라.

## 마무리
hook은 빠른 안전장치지만 만능이 아니다. 권한 자체를 끄는 일은 `/harness-perms`(allow/ask/deny)로,
규칙 안내는 CLAUDE.md로 나눠서 관리하라고 안내한다. 첫 질문부터 시작하라.
