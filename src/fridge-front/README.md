# 냉장고를 부탁해 – 프론트 초안

React + Vite. 백엔드(`localhost:8080`)의 API 전체를 화면으로 연결한 뼈대입니다.

## 실행

1. 스프링 서버와 MySQL(Docker)을 먼저 켭니다.
2. 이 폴더에서:
   ```bash
   cp .env.example .env     # 카카오를 쓸 거면 REST API 키 입력
   npm install
   npm run dev
   ```
3. http://localhost:3000 접속

## 구조

```
src/
├── api/
│   ├── client.js      토큰 헤더, 응답 data 꺼내기, 401 처리
│   ├── endpoints.js   백엔드 API 주소 모음 (컨트롤러와 1:1)
│   └── useLoad.js     화면 진입 시 데이터 불러오기
├── auth/              로그인 상태, 로그인 필요 화면 보호
├── components/        공통 UI, 소비기한 라벨 테이프
└── pages/             화면들 (fridge/ 아래는 냉장고 상세의 탭)
```

## 알아 둘 것

- **CORS 설정 없이 동작합니다.** `vite.config.js`의 proxy가 `/api` 요청을 8080으로 넘겨서,
  브라우저 입장에선 같은 주소로 요청하는 셈입니다. 배포할 땐 백엔드 CORS 설정이 필요합니다.
- **카카오 리다이렉트 주소**는 카카오 디벨로퍼스, 백엔드 `.env`, 프론트 `.env` 세 곳이 같아야 합니다.
- **카카오 콜백은 한 번만 요청합니다.** 개발 모드(StrictMode)에서 effect가 두 번 실행돼
  같은 인가 코드를 두 번 보내면 실패하기 때문입니다 (`KakaoCallbackPage.jsx`).
- 토큰은 `localStorage`에 저장합니다. 401을 받으면 토큰을 지우고 로그인 화면으로 보냅니다.
