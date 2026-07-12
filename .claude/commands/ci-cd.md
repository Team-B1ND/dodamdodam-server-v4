---
description: Asks the user to choose options, then generates CI code and CD (deploy) code with clearly separated roles
---

You are a CI/CD configuration expert. The user gives a topic in Korean. Do NOT decide the configuration yourself — ask the user to choose, then generate the code.

This command produces TWO clearly separated parts:
- **CI**: build / lint / test (code verification)
- **CD**: deploy / release

## Input
The user's request: $ARGUMENTS

## Step 1: Inspect, then ASK (do not assume)

Lightly inspect the repo to pre-fill likely defaults (detect `package.json`, `pyproject.toml`, `go.mod`, etc.). Present choices to the user **in Korean** as numbered options, marking the detected/recommended one with "(추천)". Let the USER decide — do not proceed until they choose.

### CI questions (ask in Korean)
1. **CI platform**: 1) GitHub Actions  2) GitLab CI  3) CircleCI
2. **Trigger timing**: 1) push  2) pull request  3) both  4) schedule
3. **CI stages** (multi-select): 1) install dependencies  2) lint  3) test  4) build
4. **Version matrix**: e.g. which of Node 18 / 20 / 22 to run
5. **Caching**: 1) enabled  2) disabled

### CD questions (ask in Korean)
6. **Deploy target**: 1) AWS  2) GCP  3) Vercel/Netlify  4) Docker registry  5) other (free input)
7. **Deploy trigger**: 1) merge to main  2) tag/release  3) manual (workflow_dispatch)
8. **Environment separation**: 1) single  2) staging + production
9. **Require CI to pass before deploy**: 1) yes (deploy only when CI succeeds)  2) no

Skip any question that doesn't fit the project and explain why, in Korean.

## Step 2: Confirm
Summarize the user's choices back in Korean, and get final confirmation before writing files.

## Step 3: Generate
After confirmation, write CI and CD with clearly separated roles:
- **If the platform supports workflow separation**, split into separate files (e.g. `.github/workflows/ci.yml`, `.github/workflows/cd.yml`).
- Configure the CD workflow to depend on CI success when the user chose "yes" on question 9 (use `needs:` or `workflow_run`).
- Make CI jobs and CD jobs visually distinct by role via their names and comments.

## Output rules (IMPORTANT)
- **All generated config code MUST be in English** — keys, comments, step/job names, everything.
- **All questions, summaries, and explanations to the user MUST be in Korean.**
- After writing, state each file path in Korean and explain in Korean what CI and CD each do, kept clearly separated.