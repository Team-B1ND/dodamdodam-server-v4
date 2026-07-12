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
