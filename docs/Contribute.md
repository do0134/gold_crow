# Contribute Guide

> 까마귀 공방은 다양한 재능을 가진 분들의 기여를 환영합니다.<br>
> 여러분의 기여는 저희 프로젝트에 많은 도움이 될 것입니다.

이 문서는 프로젝트 기여, 버그 보고, 디자인 변경, 번역 등 프로젝트에 기여하시려는 분들을 위한 문서입니다.

기여를 하는 것이 처음이신 분들은 [이 페이지](https://github.com/firstcontributions/first-contributions/blob/main/translations/README.ko.md)를 먼저 읽어 주세요.

<br>

# 시작하기 전

까마귀 공방에 관한 모든 작업은 GitHub(GitLab)을 통해 소통합니다.<br>

프로젝트의 작업, 개선 사항 및 버그 추적에 관련된 내용들은 **GitHub Issue**를 통해 확인해볼 수 있습니다.
새로운 Issue을 발급하기 전에 이미 논의 중인지 확인 해주세요.

<br>

# 개발 가이드

## 📑 버전 정보

까마귀 공방은 지속적이고 원활한 개발을 위해 개발 환경을 통일 하고자 합니다.<br>
다음과 같은 개발 환경 준수를 부탁 드립니다.

<table>
    <tr>
        <td>BackEnd</td>
        <td>
            <img src="https://img.shields.io/badge/java-1.8.0_342-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white" />
            <img src="https://img.shields.io/badge/spring-2.7.3-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white"/>
            <img src="https://img.shields.io/badge/mysql-8.0-%2338B2AF.svg?style=for-the-badge&logo=mysql&logoColor=white"/>
        </td>
    </tr>
    <tr>
        <td>FrontEnd</td>
        <td>
            <img src="https://img.shields.io/badge/node.js-6DA55F?style=for-the-badge&logo=node.js&logoColor=white" />
            <img src="https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB" />
            <img src="https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white" />
        </td>
    </tr>
</table>
<br>

## 🛠 포팅메뉴얼
프로젝트 관련 포팅메뉴얼은 `exec/포팅 메뉴얼.docx`에서 확인해주세요

<br>

## 📮 서버에 설치해야할 라이브러리
[requirements.txt](requirements.txt) 에 있는 라이브러리 모두 설치

<br>

## ⏳ 현재 구현되어있는 API
[API 문서](API.pdf)를 확인해주세요<br>
참고 페이지 : [Notion](https://brazen-turnover-e94.notion.site/API-7aca626b4e724623a7f6c10a82795f05)

<br>

## ✒️ 코딩 가이드

오픈소스로써, 많은 기여자 들이 원활하게 참여하고 유지 및 보수가 잘 진행될 수 있도록, Java와 Git에서 스타일 가이드를 제공합니다.

- Coding Style Guide는 [이 문서](CodeStyleGuide.md)에서 확인해주세요
- Git과 관련된 `Branch`, `Commit`, `Pull(Merge)`들의 규칙은 [이 문서](Git.md)에서 확인해주세요

<br>

## ✨ 이슈 등록 

GitHub Issue에서 자유롭게 이슈를 등록 해주세요.<br>
Issue Template을 준수하고 다음을 포함하는지 확인 해주세요

<br>

- 새로운 기능 제안
  1. 기능에 대한 요약
  2. 기능 구현 코드 설명
  3. 기능이 추가됨으로서 프로젝트가 가질 수 있는 장점
  4. 외부 라이브러리 사용 여부

<br>

- 버그 리포트
  1. 버그 요약
  2. 버그 재현을 위한 단계
  3. 버그가 발생한 버전
  4. 버그가 발생한 Directory 또는 Component

<br>

- 디자인 요소
  1. 변경하고 싶은 디자인 이미지
  2. 변경이 필요한 이유
  3. 버그 체크 여부
