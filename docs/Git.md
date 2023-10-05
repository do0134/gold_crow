# Git Guide

이 문서는 프로젝트에 기여할 때 참고하실 Git Convention입니다.

<br>

## 🔑 Branch
- **dev**
    - `feat-fe/[feature name]`
    - `fix-fe/[fix name]`

    - `feat-be/[feature name]`
    - `fix-be/[fix name]`<br>

ex) `feat-fe/signup`, `fix-be/signup`

### Branch 종류
- master : 서비스 중인 최종 버전
- dev : 배포 준비 중인 브랜치
- feat : 기능 추가 브랜치
    - feat-fe : 프론트 기능 추가 브랜치
    - feat-be : 백엔드 기능 추가 브랜치
- fix : 기능 수정 브랜치
    - fix-fe : 프론트 기능 수정 브랜치
    - fix-be : 백엔드 기능 수정 브랜치

### Merge Branch

master <- dev <- feat-fe / feat-be / fix-fe / fix-be

<br>
<br>

## 🐳 Commit Conevention
```
<타입>-<파트>: <기능명>
--------------------------
[예시]
feat-be: 백엔드 프로젝트 초기설정
fix-fe: 회원가입 수정
```
### 타입 리스트
<br>

**feat**<br>
새로운 기능 추가 (a new feature)<br>

**fix**<br>
버그 수정 (a bug fix)<br>

**docs**<br>
문서 수정 (changes to documentation)<br>

**style**<br>
코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우<br>
(formatting, missing semi colons, etc; no code change)<br>

**refactor**<br>
코드 리팩토링(refactoring production code)<br>
로직과 관계 없는 소스 코드 개선,<br>
불필요한 파일 및 코드 삭제<br>
라우트 설정 파일 변경<br>

**test**<br>
테스트 코드<br>
리펙토링 테스트 코드 추가<br>
(adding tests, refactoring test; no production code change)<br>

**chore**
<br> 빌드 업무 수정, 패키지 매니저 수정, 라이브러리, 환경 설정 파일(package.json, .config 등) 수정 등<br>
updating build tasks, package manager configs, etc; no production code change<br>


**design**<br>CSS 등 사용자 UI 디자인 변경

**comment**<br>필요한 주석 추가 및 변경

**rename**<br>파일 또는 폴더명을 수정하거나 옮기는 작업만 수행한 경우

**remove**<br>파일을 삭제하는 작업만 수행한 경우

**!BREAKING CHANGE**<br>API의 큰 변경인 경우

**!HOTFIX**<br>급하게 치명적인 버그를 고쳐야 하는 경우

<br>

## 📌 Pull Convention

Merge Request == Pull Request == MR == PR

```
[GitLab 이슈번호] <타입> : <기능명>
--------------------------
[예시]
[#13] feat : 백엔드 프로젝트 초기설정
[#14] fix : Swagger 작동 안되는 문제 수정
```
### 타입 리스트
<br>

**feat**<br>
새로운 기능 추가 (a new feature)<br>

**fix**<br>
버그 수정 (a bug fix)<br>

**docs**<br>
문서 수정 (changes to documentation)<br>

**style**<br>
코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우<br>
(formatting, missing semi colons, etc; no code change)<br>

**refactor**<br>
코드 리팩토링(refactoring production code)<br>
로직과 관계 없는 소스 코드 개선,<br>
불필요한 파일 및 코드 삭제<br>
라우트 설정 파일 변경<br>

**test**<br>
테스트 코드<br>
리펙토링 테스트 코드 추가<br>
(adding tests, refactoring test; no production code change)<br>

**chore**
<br> 빌드 업무 수정, 패키지 매니저 수정, 라이브러리, 환경 설정 파일(package.json, .config 등) 수정 등<br>
updating build tasks, package manager configs, etc; no production code change<br>


**design**<br>CSS 등 사용자 UI 디자인 변경

**comment**<br>필요한 주석 추가 및 변경

**rename**<br>파일 또는 폴더명을 수정하거나 옮기는 작업만 수행한 경우

**remove**<br>파일을 삭제하는 작업만 수행한 경우

**!BREAKING CHANGE**<br>API의 큰 변경인 경우

**!HOTFIX**<br>급하게 치명적인 버그를 고쳐야 하는 경우