---
description: Create a new git branch following the repository's branch naming convention
argument-hint: [optional hint about the work, e.g. "login bug fix"]
allowed-tools: Bash(git branch:*), Bash(git checkout:*), Bash(git for-each-ref:*), Bash(git config:*), Bash(git symbolic-ref:*)
---

# Create Branch

Help the user create a new git branch that follows the repository's branch naming
convention. Work through the three steps below in order. **All explanatory output,
questions, and confirmations shown to the user MUST be written in Korean.** Only the
final branch name itself is in English (kebab-case ASCII).

## Context (read before acting)

- Existing branches: !`git for-each-ref --sort=-committerdate --count=30 --format='%(refname:short)' refs/heads/`
- Current branch: !`git symbolic-ref --short HEAD 2>/dev/null || echo "(detached)"`
- Configured convention (if any): !`git config --get branch.convention 2>/dev/null || echo "(none set)"`
- User's optional hint: $ARGUMENTS

## Step 1 — Establish the branch convention (확인 단계)

Determine the naming convention to follow, in this priority order:

1. If the user explicitly states a convention in this conversation, use it.
2. Else, if `branch.convention` is set in git config, use that.
3. Else, **analyze the existing branch list above** and infer the dominant pattern.
   Look for: a type prefix (`feat/`, `fix/`, `chore/`, `docs/`, `refactor/`,
   `hotfix/`...), a separator (`/`, `-`, `_`), word casing (kebab-case is typical),
   and whether ticket IDs are embedded (e.g. `feat/PROJ-123-...`).

Then present the convention you will follow back to the user **in Korean**, citing
what you observed, and ask them to confirm or correct it. Example phrasing:

> 기존 브랜치를 분석한 결과, `<type>/<kebab-case-설명>` 형태의 컨벤션을 사용하시는 것으로 보입니다.
> (예: `feat/user-login`, `fix/token-expiry`)
> 이 규칙으로 진행할까요? 다른 컨벤션을 원하시면 알려주세요.

If no existing branches reveal a clear pattern, propose a sensible default
(`<type>/<short-description>` with kebab-case) and ask the user to confirm — still
in Korean. Do not proceed until the convention is agreed.

## Step 2 — Collect the required details (정보 입력 단계)

Once the convention is confirmed, ask the user — **in Korean** — for the specific
pieces the convention needs. Ask only for what is actually required by the agreed
pattern, for example:

- 작업 유형 (예: feat / fix / chore / docs / refactor)
- 브랜치에 담을 작업에 대한 짧은 설명
- (컨벤션에 티켓 번호가 포함된다면) 이슈/티켓 번호

Use the user's `$ARGUMENTS` hint to pre-fill or suggest answers where possible, but
still confirm. Then construct the final branch name in English (ASCII kebab-case),
sanitizing the description: lowercase, spaces → hyphens, strip characters invalid in
git ref names. Show the assembled name to the user and confirm — in Korean:

> 생성할 브랜치 이름: `feat/user-login`
> 이대로 생성하겠습니다.

## Step 3 — Create the branch (생성 단계)

Before creating, verify the name does not already exist (check against the branch
list). If it collides, tell the user in Korean and ask for an alternative.

Create and switch to the new branch:

```
git checkout -b <branch-name>
```

Then report success **in Korean**, for example:

> ✅ `feat/user-login` 브랜치를 생성하고 전환했습니다.

## Rules

- Branch names are always English, kebab-case, ASCII only.
- Every message the user reads (questions, confirmations, results) is in Korean.
- Never create the branch without the user confirming both the convention (Step 1)
  and the final name (Step 2).
- Never force-create or overwrite an existing branch.