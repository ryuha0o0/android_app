# Labbit 보안 실습(Security Lab) 가이드

Labbit은 금융보안원 전자금융기반시설 평가기준 등 점검항목을 **직접 진단**해 보는
교육용 앱입니다. 각 취약 시나리오는 `VulnConfig` 플래그로 **취약 모드 ↔ 안전 모드**를
전환할 수 있어, 같은 기능을 두 모드로 비교하며 취약점을 찾고 수정 효과를 확인합니다.

- 설정 위치: 앱 실행 → **마이페이지 → 보안 실습 설정**
- 전환: 스위치 **ON = 취약**, **OFF = 안전**. "전체 안전/취약 모드" 버튼으로 일괄 전환
- 기본값: 모든 시나리오 **취약 모드 ON** (진단 대상)

> ⚠️ 이 앱은 교육용이며 실제 서버·실제 금융데이터가 없습니다. 모든 처리는 기기 내부(로컬)
> 에서만 일어납니다. 운영 배포 시에는 `security/VulnConfig.kt`와 각 `vulnerable` 분기를
> 반드시 제거해야 합니다.

## 범위 안내 (서버 없음)
점검항목에는 **서버 측(B 항목, 서버 입력값 검증, 서버 세션/HTTPS 구성 등)** 이 많습니다.
현재는 서버가 없어 **앱(클라이언트) 측에서 재현 가능한 항목**을 구현했고, 일부 취약점은
로컬 SQLite·로컬 파일·WebView로 동형(同型) 재현했습니다(예: 로컬 SQL 인젝션, 로컬 경로추적).
순수 서버 항목(실제 TLS 핸드셰이크, 서버 BOLA/IDOR, 관리자페이지 노출 등)은 추후 실습용
로컬 백엔드를 붙일 때 `docs/api/openapi.yaml` 명세 위에서 구현합니다.

## 구현 매핑

### 앱 실행 / 공통 인프라 (P0)
| 시나리오 | VulnConfig 플래그 | 구현 위치 | 점검번호 |
| --- | --- | --- | --- |
| 자동로그인 자격증명 평문 저장 | `PLAINTEXT_AUTOLOGIN` | `security/SecureStore.kt` (평문 vs EncryptedSharedPreferences) | D-26,27 / A-14,16 |
| 유추 가능한 기기 UUID | `PREDICTABLE_UUID` | `security/SessionManager.deviceUuid()` (시간기반 vs randomUUID) | A-14 / B-10 |
| 기기 바인딩 미검증(토큰 재사용) | `NO_DEVICE_BINDING` | `security/SessionManager.tryAutoLogin()` | C-07,08 / D-62 |
| 예측 가능한 세션 ID | `WEAK_SESSION_ID` | `security/SessionManager.newSessionId()` (순차 vs 128bit 난수) | D-22,30 / B-10 |
| 로그인 시도 제한 없음 | `NO_LOGIN_LOCKOUT` | `security/SessionManager` (5회 잠금) | D-49 / B-12 |
| 세션 타임아웃 없음 | `NO_SESSION_TIMEOUT` | `security/SessionManager.isExpired()` (유휴 10분) | D-30,50 |
| 로그아웃 세션 무효화 | (안전 고정) | `security/SessionManager.logout()` + `MoaApp` onLogout | D-50,62 |
| 무결성·루팅·디버깅·에뮬레이터 탐지 | `SKIP_INTEGRITY_CHECK` | `security/DeviceIntegrity.kt` + `MainActivity` | D-07,14~16 / A-09,20 |
| 화면 캡처·백그라운드 보호 | `ALLOW_SCREEN_CAPTURE` | `MainActivity.applyScreenCapturePolicy()` (FLAG_SECURE) | D-38,60 |
| 하드웨어 보안키(생체=개인키) | (안전 고정) | `security/KeystoreManager.kt` (Keystore EC 서명) | D-22,23,63 |
| TLS·cleartext 정책 | (안전 고정) | `res/xml/network_security_config.xml`, Manifest `usesCleartextTraffic=false` | D-47,51~54 / B-15 |
| 과도한 권한·컴포넌트 접근통제 | (안전 고정, 점검 대상) | `AndroidManifest.xml` (권한 최소화, 내부 화면 비노출) | A-03,12 / C-21,24 |
| 백업 민감정보 제외 | (안전 고정) | `res/xml/data_extraction_rules.xml`, `allowBackup=false` | D-26 |
| 금융인증서 로그인 | (안전 고정) | `security/CertificateManager.kt`+`KeystoreManager.kt`+`MockBackend.kt` (챌린지-응답 서명) | D-01~03,22,23,63 |
| 간편비번 약한 저장 | `WEAK_PIN_STORAGE` | `security/PinManager.derive()` (솔트없는 해시 vs PBKDF2 10만회) | D-24,27 / C-10 |
| 간편비번 시도 제한 없음 | `NO_PIN_LOCKOUT` | `security/PinManager.verify()` (5회 잠금) | D-49 / B-12 |
| 고정 보안키패드 | `STATIC_KEYPAD` | `ui/components/SecureKeypad.kt` (배열 셔플 vs 고정) | D-24 |
| E2E 필드 암호화 미적용 | `NO_E2E_ENCRYPTION` | `security/E2ECrypto.kt`+`MockBackend.e2eDecrypt` (민감필드 평문 vs RSA+AES-GCM) | D-47,51 / B-15 |

### 핵심 금융 기능 (P1)
| 시나리오 | VulnConfig 플래그 | 구현 위치 | 점검번호 |
| --- | --- | --- | --- |
| 거래내역 날짜 조회 SQL 인젝션 | `SQLI_TRANSACTION` | `data/local/MoaDbHelper.searchTransactions()` + `InquiryScreen` 거래내역 탭 | D-19 / B-02 / C-01 |
| 이체 파라미터 변조(한도·소유 재검증) | `PARAM_TAMPER_TRANSFER` | `data/MoaRepository.executeTransfer()` + `TransferScreen` "요청 직접 전송" | D-04,21 / B-08,17 |
| 이체 메모 XSS | `WEBVIEW_XSS` | `ui/components/LabWebView.kt` (escape 토글) | D-58 / C-03 |
| 추가인증 Challenge·서명 | (안전 고정) | `security/KeystoreManager.sign()` (1회성 challenge 서명) | D-01~03,22,23 |
| 비밀번호 변경 본인확인 단계 우회 | `AUTH_STEP_BYPASS` | `ui/screens/EditProfileScreen.kt` PasswordTab + 딥링크 `labapp://password-change` | D-05,37,44 |
| 전체계좌/상세 소유주 검증 | (안전 고정, 로컬) | `data/MoaRepository` (내 계좌만 보유) | D-04,21 |

### 메인창 / 확장 (P1·P2)
| 시나리오 | VulnConfig 플래그 | 구현 위치 | 점검번호 |
| --- | --- | --- | --- |
| 검색창 XSS | `WEBVIEW_XSS` | `ui/screens/MainBoardScreen.kt` 검색 탭 + `LabHtml.field()` | D-58 / C-03 / B-06 |
| 공지/게시글 저장형 XSS | `WEBVIEW_XSS` | `MainBoardScreen` 공지·게시판 + `LabWebView` | D-58 / C-03 |
| 고객센터·상태변경 CSRF 토큰 | `NO_CSRF_TOKEN` | `MainBoardScreen` `LabBridge.action()` (토큰 검증) | D-45 / B-11 |
| 게시판 파일 업로드 검증 | `UNSAFE_FILE_UPLOAD` | `security/FileVault.upload()` (확장자 화이트리스트) | D-20 / B-05,13 |
| 다운로드 경로추적(Path Traversal) | `PATH_TRAVERSAL` | `security/FileVault.download()` (canonicalPath 검증) | D-28 / B-14 / C-02 |

### 참고: 실습 재현 예시
- **SQL 인젝션**: 거래내역 탭의 시작/종료일에 `2026-10-01` / `2026-12-31' OR '1'='1` 입력 →
  취약 모드면 다른 계좌 거래까지 조회됨(경고 배너 표시).
- **파라미터 변조**: 이체 화면 "요청 직접 전송(실습)" → `amount`에 `9000000`(한도 100만 초과),
  `fromAccountId`에 `install`(비이체 적금) 입력 → 취약 모드면 실행, 안전 모드면 거부.
- **XSS**: 검색어에 `<img src=x onerror="LabBridge.report('XSS','검색창')">` 입력 →
  취약 모드면 실행 배너, 안전 모드면 escape 되어 그대로 텍스트 노출.
- **경로추적**: 게시판 다운로드 파일명에 `../databases/moa_lab.db` 입력 →
  취약 모드면 앱 내부 DB 파일이 읽힘, 안전 모드면 차단.
- **인증단계 우회**: `adb shell am start -a android.intent.action.VIEW -d "labapp://password-change"` →
  취약 모드면 로그인/본인확인 없이 비밀번호 변경 화면 진입.
- **화면 캡처**: `ALLOW_SCREEN_CAPTURE` OFF 시 스크린샷/최근 앱 미리보기 차단(FLAG_SECURE).

## 알려진 범위/한계
- 거래내역 탭은 로컬 DB(`moa_lab.db`)를 조회하므로, 세션 중 발생한 이체는 즉시 반영되지
  않습니다(인메모리 `MoaRepository`와 분리). SQLi 실습 표면을 분명히 하기 위한 의도입니다.
- 탐지 로직(루팅/후킹)은 단순 지표 기반이며 상용 수준이 아닙니다. 방어선 개념 학습용입니다.
