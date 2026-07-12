## 📌 Commit Guide
현재 이 프로젝트의 변경점을 찾아 Git 컨벤션에 맞게 커밋합니다.

---

## ✨ Commit Format
```
<type>: <subject>
```

---

## 🏷 Type 목록
```
feat     : 새로운 기능 추가
fix      : 버그 수정
docs     : 문서 수정 (README 등)
style    : 코드 스타일 변경 (포맷팅, 세미콜론 등, 로직 변경 없음)
refactor : 코드 리팩토링 (기능 변화 없음)
test     : 테스트 코드 추가/수정
chore    : 빌드, 설정 파일 수정
perf     : 성능 개선
ci       : CI/CD 관련 수정
```

---

## 📝 Subject 규칙
- 50자 이내
- 첫 글자 소문자
- 마침표(.) 금지
- 명령형으로 작성 (added ❌ → add ⭕)

---

## ✅ Commit 예시

```
feat: add user login API
fix: resolve null pointer exception in auth service
```

---

## 🚀 Push Guide

### 1. 기본 규칙
- 커밋 후 반드시 원격 저장소에 push
- 하나의 작업 단위로 커밋 후 push 권장
- 불필요한 커밋은 squash 후 push
- 브렌치를 하나 만든 후 그 브렌치에 checkout하여 커밋을 한다.

---

## 🌿 Branch Naming 규칙

### 1. 기본 형식
```
<type>/<description>
```

### 2. 예시
```
feature/login-api
fix/auth-token-error
refactor/user-service
docs/readme-update
```

### 3. 작성 규칙
- 소문자만 사용
- 공백 대신 하이픈(-) 사용
- 너무 길지 않게 작성
- 어떤 작업인지 명확하게 드러나도록 작성

```

---

### 2. Push 명령어
```
git push origin <branch-name>
```

예시:
```
git push origin feature/login-api
```

---

### 3. Force Push 규칙
- 협업 브랜치에서는 사용 금지
- 개인 작업 브랜치에서만 사용
- 사용 시 팀원에게 공유 필수

```
git push --force
```

---

### 4. Push 전 체크리스트
- [ ] 테스트 코드 실행 완료
- [ ] 불필요한 코드 제거
- [ ] 민감 정보 포함 여부 확인
- [ ] 커밋 메시지 컨벤션 준수

---

### 5. PR 생성 규칙
- push 후 Pull Request 생성
- 작업 내용 명확하게 작성
- 리뷰어 지정 필수
```
