---
description: Clean up local git branches — ask which local branches to keep, then delete the rest
argument-hint: [optional branch names to keep, space-separated]
allowed-tools: Bash(git branch:*), Bash(git symbolic-ref:*), Bash(git for-each-ref:*)
disable-model-invocation: true
---

# Context

- All LOCAL branches: !`git branch --format='%(refname:short)'`
- Current branch (checked out, must NOT be deleted): !`git symbolic-ref --short HEAD 2>/dev/null || echo "(detached HEAD)"`
- User's keep hint (if any): $ARGUMENTS

# Task

Clean up **local** git branches only. This NEVER touches remote branches. Work
through the steps below in order. **All questions and confirmations shown to the user
MUST be in Korean.**

## Step 1 — Ask which local branches to keep (남길 브랜치 확인)

Look at the local branch list above. Present it to the user **in Korean** and ask
which local branches they want to KEEP. If `$ARGUMENTS` already names branches to
keep, treat those as the answer but still confirm. Example phrasing:

> 현재 로컬 브랜치는 다음과 같습니다: <목록>
> 어떤 로컬 브랜치를 남길까요? 남길 브랜치 이름을 알려주세요.

Do not proceed until the user has told you what to keep.

## Step 2 — Compute and confirm the delete list (삭제 목록 확인)

From the full local branch list, the branches to delete are everything EXCEPT the
ones the user chose to keep. Before deleting, ALWAYS protect these by removing them
from the delete list even if the user didn't explicitly keep them:

- The **current branch** (the checked-out one shown in Context). Git can't delete it
  anyway, and it must never be proposed for deletion.
- `main` and `master`, unless the user *explicitly* names one for deletion.

Show the user the resulting delete list **in Korean** and ask for explicit
confirmation, because deleting branches is hard to undo. Example:

> 삭제할 로컬 브랜치: <목록>
> (남길 브랜치: <목록>)
> 이대로 삭제할까요?

If the delete list is empty, tell the user there's nothing to delete and stop.

## Step 3 — Delete the remaining local branches (삭제 실행)

Only after explicit confirmation, delete each branch in the list:

```
git branch -d <branch-name>
```

Use `-d` (safe delete), which refuses to remove branches that aren't fully merged.
If git reports a branch is not fully merged, do NOT silently force-delete it — tell
the user **in Korean** which branch was skipped and that it has unmerged commits, and
ask whether they want to force-delete it with `git branch -D`. Only run `git branch -D`
on a branch after the user confirms that specific branch.

## Step 4 — Report (결과 보고)

Report the result **in Korean**: which branches were deleted, which were kept, and
any that were skipped because they weren't fully merged. Example:

> ✅ 삭제 완료: <목록>
> 유지: <목록>
> ⚠️ 건너뜀(병합 안 됨): <목록>

## Rules

- LOCAL branches only — never delete or modify remote branches.
- Never delete the current branch, `main`, or `master` without explicit user intent.
- Always confirm the delete list before deleting (deletion is hard to reverse).
- Prefer safe delete (`-d`); only force-delete (`-D`) a branch the user specifically
  approves.
- Every message the user reads is in Korean.
