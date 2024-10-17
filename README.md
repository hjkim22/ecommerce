# 🚀 ecommerce 
> 사용자와 판매자 간의 원활한 거래를 지원하는 Spring Boot 기반 커머스 플랫폼 프로젝트

## 🛠️ Tech Stack
- **Language**: `Java 17`
- **Framework**: `Spring Boot 3.3.4`
- **Build Tool**: `Gradle 8.10.2`
- **Database**: `MySQL`
- **Caching**: `Redis`
- **Containerization**: `Docker`
- **Cloud**: `AWS`
- **Libraries**:
    - `JPA`
    - `Security`
    - `Validation`
    - `Web`
    - `mail`
    - `jjwt`
    - `Lombok`

## ⛓️ 프로젝트 구조 및 기능

- **회원가입 및 로그인**
    - 이메일 인증을 통한 회원가입 프로세스
    - JWT를 이용한 로그인 인증 및 예외 처리

- **상품 관리**
    - 판매자가 상품을 등록하고 수정할 수 있는 기능
    - 사용자가 상품을 검색하고 상세 정보를 볼 수 있는 기능

- **장바구니 기능**
    - 사용자가 장바구니에 상품을 추가하고 수정할 수 있는 기능
    - 장바구니에서 상품을 삭제할 수 있는 기능
    - 로그인 된 회원만 장바구니 기능 사용 가능

- **주문 처리**
    - 사용자가 상품을 주문하는 기능 
    - 주문 내역을 확인할 수 있는 기능

## ERD
![ecommerceTest3](https://github.com/user-attachments/assets/bd525d63-4648-41d0-81a5-8fb5857d8d56)

<br>

#### 추가 고려 사항

- 결제 프로세스 구현
- 리뷰 기능 구현
- 타임리프 사용