# Java, JavaScript, DB의 Style Guide

## Style Guide의 목표 및 목적
---
기여자, 사용자 모두 코드와 데이터베이스의 형태 파악과<br>데이터베이스에 포함한 모든 개체가 가진 목적을 쉽게 파악하게 하기 위해 정한 Guide

<br>

## Code Convention

---

Java와 JavaScript는 구글 Style Guide를 준수합니다.
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Google JavaScript Style Guide](https://google.github.io/styleguide/jsguide.html)

<br>

## Data Style Guide
---
<br>

### 공통
- 30글자 이내의 이름을 가진다.
    - 간결할수록 좋다.
- 약어는 사용하지 않는다.
    - 오역을 가져다 줌으로 피한다.
- 공백은 사용하지 않는다.
    - 시스템이 허용해도 공백은 사용하지 않는다
- 영문만을 사용한다. 
    - 영문외에 한글이나 다른 글자를 사용하면 사용하는데 어려움이 있다.
    - 특수문자도 사용하는데 어려움
- 두개의 글자를 합한 형태의 글자는 피한다.
    - 어떤 두 문자어는 하나 이상의 의미를 가질수 있기 때문이다.
- 읽을 수 있는 이름으로 정한다.

<br>

### Table
- 간결하게 사용한다.
    - 객체의 일부분이 테이블의 이름을 참고하여 지을 수 있도록
    - 어떤 시스템(오라클 30글자)에서는 글자수 제한이 있다
- 테이블명은 단수형을 사용한다
- 소문자만을 사용하며 두글자 이상의 단어는 ‘_’을 이용하는 **snake_case** 방식을 사용한다.
- 공백문자나 약어는 사용하지 않는다

<br>

### Columns
- auto increment 속성의 PK를 대리키로 사용하는 경우, "테이블 이름"Seq 의 규칙으로 명명한다.
- 이름을 구성하는 각각의 단어(첫단어 제외)의 첫 문자를 대문자로 연결하는 **camelCase**를 사용한다.
- foreign key 컬럼은 부모 테이블의 primary key 컬럼 이름을 그대로 사용한다.
- 달라질 경우 참조하는 컬럼을 헷갈릴 수 있다.
boolean 필드인 경우 'is' 접두어를 사용한다.
    - 컬럼명만으로 데이터 타입을 알기 위해
- date, datetime 유형의 컬럼이면 "At" 접미어를 사용한다.
    - 컬럼명만으로 데이터 타입을 알기 위해