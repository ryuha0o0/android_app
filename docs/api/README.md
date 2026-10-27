# Labbit API 명세 (초안) · v1.1

서버가 아직 없어서 **명세만** 정의한 상태입니다. 전체 스펙은 [`openapi.yaml`](openapi.yaml)(OpenAPI 3.0)에 있고,
[Swagger Editor](https://editor.swagger.io)에 붙여 넣으면 문서로 보거나 서버 코드를 생성할 수 있습니다.

## 공통 규칙
- Base URL: `https://api.moa.example/v1` (가칭)
- 인증: `Authorization: Bearer <accessToken>` — 로그인·회원가입·기준정보 API만 예외
- 금액: 원 단위 정수(int64). 거래내역의 `amount`는 입금 양수, 출금 음수
- 날짜 `YYYY-MM-DD`, 일시 ISO-8601(+09:00)
- 계좌·카드번호는 응답에서 항상 마스킹 (`110-245-••7890`)
- 이체·출금 요청은 `Idempotency-Key`(UUID) 헤더 필수 — 네트워크 재시도로 두 번 이체되는 것을 막음
- 목록은 커서 페이지네이션: `?cursor=&size=` → 응답 `nextCursor`
- 오류 응답: `{ "code": "INSUFFICIENT_BALANCE", "message": "이체 가능 금액을 초과했어요", "traceId": "..." }`
  — `message`는 그대로 화면에 보여줄 수 있는 문구

## 화면별 API
| 화면 | 기능 | API |
| --- | --- | --- |
| 로그인 | 금융인증서 로그인 | `POST /auth/challenge` → `POST /auth/login/certificate` |
| | 생체인증 로그인 | `POST /auth/challenge` → `POST /auth/login/biometric` |
| 회원가입 | 약관 목록 | `GET /terms?context=SIGNUP` |
| | 본인 확인 요청 → 인증번호 확인 | `POST /signup/identity-verifications` → `POST /signup/identity-verifications/{id}/confirm` |
| | 가입 완료 | `POST /signup` |
| 홈 | 인사말·내 정보 | `GET /users/me` |
| | 총 잔액·내 계좌·금융기관 필터 | `GET /accounts?institutionCode=` |
| | 새로고침 | `POST /accounts/refresh` |
| | 보안 상태 | `GET /security/status` |
| 조회 > 전체계좌 | 계좌 목록, 적금 상세보기 | `GET /accounts`, `GET /accounts/{id}` |
| 조회 > 거래내역 | 기간·입금/출금·정렬 | `GET /accounts/{id}/transactions?from=&to=&type=&sort=` |
| 조회 > 이체결과 | 목록·상세·이체확인증 | `GET /transfers`, `GET /transfers/{id}`, `GET /transfers/{id}/receipt` (PDF) |
| 조회 > 해지계좌 | 기간별 해지계좌 | `GET /closed-accounts?from=&to=` |
| 이체 | 은행 목록 | `GET /institutions` |
| | 최근 / 자주 쓰는 | `GET /transfers/recent-recipients` |
| | 받는 분 확인 (예금주 조회) | `POST /transfers/recipient-inquiry` |
| | 한도 안내 | `GET /transfers/limits?accountId=` |
| | 이체 실행 | `POST /transfers` |
| 출금 | 출금 코드 발급 / 취소 | `POST /withdrawals/cardless`, `DELETE /withdrawals/cardless/{id}` |
| 카드 > 카드 신청 | 상품 정보, 신청 | `GET /card-products`, `POST /card-applications` |
| 카드 > 내 카드 관리 | 카드 정보 | `GET /cards` |
| | 이용금액·예상 캐시백 | `GET /cards/{id}/usage-summary?month=2026-10` |
| | 카드 이용내역 | `GET /cards/{id}/transactions` |
| | 결제계좌 관리 | `PUT /cards/{id}/settlement-account` |
| | 일시정지 | `PUT /cards/{id}/suspension` |
| | 분실신고 / 재발급 | `POST /cards/{id}/loss-reports` |
| 마이페이지 | 내 정보, 알림 설정, 로그아웃 | `GET /users/me`, `GET·PUT /users/me/notification-settings`, `POST /auth/logout` |
| | 기기 생체인증 등록 / 해제 | `POST·DELETE /auth/biometric` |
| 회원정보 수정 | 기본정보 / 비밀번호 변경 | `PATCH /users/me`, `PUT /users/me/password` |
| 계좌 관리 | 계좌 비밀번호 변경 | `PUT /accounts/{id}/password` |
| | 계좌 별명 변경 | `PATCH /accounts/{id}` |
| | 대표계좌 설정 | `PUT /accounts/primary` |
| | 한도제한 정보 / 해제 신청 | `GET /accounts/{id}/limit`, `POST /accounts/{id}/limit-release-requests` |
| | 연결 해제 | `DELETE /accounts/{id}` |

| 메인 — 검색창 | 통합검색 | `GET /search?q=` |
| 메인 — 공지사항 | 공지 목록·상세 | `GET /notices`, `GET /notices/{id}` |
| 메인 — 게시판 | 글 목록·작성 | `GET·POST /posts` |
| 메인 — 게시판 첨부 | 업로드·다운로드 | `POST /posts/{id}/attachments`, `GET /attachments/{fileId}` |
| 메인 — 고객센터 | 문의 목록·등록 | `GET·POST /inquiries` |
| 마이페이지 — 기기 관리 | 바인딩 기기 목록·등록·해제 | `GET·POST /devices`, `DELETE /devices/{id}` |
| 마이페이지 — 세션 관리 | 활성 세션·강제 종료 | `GET /sessions`, `DELETE /sessions/{id}` |
| 로그인 — 비밀번호 재설정 | OTP 요청·확인 | `POST /auth/password-reset`, `POST /auth/password-reset/confirm` |

## 이체 흐름
1. `POST /transfers/recipient-inquiry` — 은행·계좌번호로 예금주(`김하늘`)를 확인하고 `inquiryId`를 받음 (5분 유효)
2. 화면에서 "김하늘님께 50,000원을 보낼까요?" 확인
3. `POST /transfers/challenge` — 거래정보를 묶은 1회성 challenge 발급 (API27)
4. `POST /transfers` — `inquiryId`, 금액, 기기키 결합서명(challenge 서명, API28), `Idempotency-Key`를 보냄
5. 응답의 `TransferResult`로 이체 완료 화면을 그림 (조회 > 이체결과와 같은 모델)

대표 오류: `INSUFFICIENT_BALANCE`(잔액 부족), `LIMIT_EXCEEDED`(한도 초과), `INQUIRY_EXPIRED`(예금주 조회 만료), `WRONG_PASSWORD`, `DUPLICATE_REQUEST`

## 앱 연결 계획 (서버가 생기면)
현재 앱은 서버 없이 로컬(메모리·SQLite·파일)로 동작하는 보안 실습 앱입니다. 취약/안전 토글과 점검항목 매핑은 `docs/security/SECURITY-LAB.md` 참고.
이 명세는 그 로컬 동작을 실제 서버 API로 옮길 때의 계약(안전한 기준)을 정의합니다. 서버가 준비되면
Retrofit 인터페이스와 DTO를 이 명세에서 만들고, `MoaRepository`의 각 함수를 해당 API 호출로 바꾸면 됩니다.
