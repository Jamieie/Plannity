# 📅 Plannity
> 사용자의 일정을 관리할 수 있는 캘린더 기반 일정/할일 관리 서비스

## 📌 프로젝트 개요
- 일정(Event)과 할 일(Task)을 연동하여 한 눈에 볼 수 있는 일정 관리 시스템
- 개인 일정, 종일 일정, 반복 일정 등 다양한 이벤트 유형을 지원

### ✨ 주요 기능 (구현 중)
- 사용자(회원)
  - 🔐 **사용자 인증**: JWT 토큰 기반 회원가입/로그인
- 스케줄(일정 및 할일)
  - 📅 **캘린더 뷰**: 일정 및 할일을 캘린더에 표시, 검색
  - ✅ **일정/할일 관리**: 일정 및 할일 생성/수정/삭제, 할일 상태 설정, 하위 할일 설정
  - 🔗 **일정 및 할일 연동**: 할일을 관련된 일정에 지정 가능 
  - 📋 **일정 및 할일 리스트**: 일정 및 할일을 그룹별로 관리

## 🛠️ 기술 스택
### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.4.4, Spring Security 6.x
- **Database**: MySQL 8.0, Spring Data JPA
- **Authentication**: JWT Token
- **Validation**: Bean Validation
- **Testing**: JUnit 5, Mockito

### Tools & Environment
- **IDE**: IntelliJ IDEA
- **Build Tool**: Gradle
- **API Testing**: Postman
- **Documentation**: Swagger/OpenAPI
- **Database Design**: ERD Cloud
- **Version Control**: Git

## 🏗️ 프로젝트 구조
```bash
    src/main/java/org/mi/plannitybe/
    ├── common/           # 공통 컴포넌트 (BaseEntity, 예외 처리)
    ├── config/           # 설정 클래스 (Security, JPA Auditing)
    ├── exception/        # 비즈니스 예외 정의 및 예외 처리
    ├── schedule/         # 일정 관리 도메인
    │   ├── controller/   # REST API 컨트롤러
    │   ├── service/      # 비즈니스 로직
    │   ├── repository/   # 데이터 접근 계층
    │   ├── entity/       # JPA 엔티티
    │   └── dto/          # 데이터 전송 객체
    ├── user/            # 사용자 관리 도메인
    ├── policy/          # 약관 관리 도메인
    └── jwt/             # JWT 토큰 처리
```

## 📄 프로젝트 문서
- [요구사항 정의서](https://docs.google.com/spreadsheets/d/1R9gOoRJNYAK6NDnYUCXdQ8l59dBfsWaAMGG1oECogOk/edit?usp=sharing) - 사용자 및 기능 요구 정리
- [기능명세서](https://docs.google.com/spreadsheets/d/1UaQquMWnfwB13PB51PrqXKV7bqJbH7JT-8f1behAz5k/edit?usp=sharing) - 기능별 세부 동작 설명
- [ERD](https://www.erdcloud.com/p/f9qnhELEzCbtTTkw8) - DB 구조 시각화
- [API 명세서](https://docs.google.com/spreadsheets/d/1XooI-cQLLd4ZoHj87nINlq2w8xSMEfXUipe_3My7txg/edit?usp=sharing) - 클라이언트 통신 API 정리

## 🔮 향후 계획
- 반복 일정 기능 구현
- 알림 기능 추가
- 소셜 로그인 추가
- 통계 기능 추가