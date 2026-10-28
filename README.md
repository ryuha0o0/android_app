# Labbit — 금융 보안 실습 앱

Figma 디자인을 바탕으로 만든 Kotlin + Jetpack Compose 안드로이드 금융 앱이자,
금융보안원 전자금융기반시설 평가기준 등 점검항목을 **직접 진단해 보는 교육용 앱**입니다.
(브랜드명은 Labbit, 내부 패키지/코드 식별자는 `com.moa.finance` 그대로입니다.)

## 실행 방법
1. Android Studio(Ladybug 이상)에서 이 폴더를 **Open** 합니다.
2. Gradle Sync가 끝나면 에뮬레이터나 기기를 선택하고 ▶ Run 을 누릅니다.

- compileSdk / targetSdk 35, minSdk 26, JDK 17
- AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Navigation Compose 2.8.5
- 보안 라이브러리: security-crypto(암호화 저장소), biometric, webkit

## 화면 구성
| 화면 | 파일 |
| --- | --- |
| 로그인 (금융인증서 / 간편비밀번호) | `ui/screens/LoginScreen.kt` |
| 간편비밀번호 등록·로그인 (보안 키패드) | `ui/screens/PinScreen.kt`, `ui/components/SecureKeypad.kt` |
| 회원가입(본인 확인) | `ui/screens/SignUpScreen.kt` |
| 홈 | `ui/screens/HomeScreen.kt` |
| 조회 (전체계좌 / 거래내역 / 이체결과 / 해지계좌) | `ui/screens/InquiryScreen.kt` |
| 이체 / 출금 | `ui/screens/TransferScreen.kt` |
| 카드 (카드 신청 / 내 카드 관리) | `ui/screens/CardScreen.kt` |
| 메인창 (검색 / 공지 / 게시판 / 고객센터) | `ui/screens/MainBoardScreen.kt` |
| 마이페이지 | `ui/screens/MyPageScreen.kt` |
| 회원정보 수정 (기본정보 / 비밀번호 변경) | `ui/screens/EditProfileScreen.kt` |
| 계좌 관리 | `ui/screens/AccountManageScreen.kt` |
| 보안 실습 설정 | `ui/screens/SecurityLabScreen.kt` |

내비게이션과 하단 탭은 `ui/navigation/MoaApp.kt`, 공통 컴포넌트는 `ui/components/`,
색상은 `ui/theme/Color.kt`, 보안 관련 코드는 `security/` 에 있습니다.

## 데이터 / 동작
서버 없이 동작합니다. 계좌·거래·카드 등은 `data/MoaRepository.kt`(인메모리)와 `data/local/MoaDbHelper.kt`
(거래내역 SQLite)로, 게시판·공지·문의는 `data/BoardRepository.kt`로 동작합니다.
이체·출금을 하면 잔액·거래내역·이체결과가 실제로 갱신되고, 대표계좌 설정·별명 변경·카드 일시정지 등도
앱 안에서 반영됩니다. 실제 서비스로 연결하려면 이 저장소들을 `docs/api/openapi.yaml` 기반 API 호출로 교체하면 됩니다.

## 인증 구조 (금융인증서 · 간편비밀번호 · E2E)
- **금융인증서 로그인**: 가입 시 기기 Keystore에 EC 키쌍을 만들고(하드웨어 보호, 추출 불가) 공개키를 서버에 등록,
  로그인 때 1회성 챌린지를 개인키로 서명해 검증합니다(챌린지-응답). `security/CertificateManager.kt`
- **간편비밀번호(6자리)**: 최초 인증서 로그인 후 등록하면 이후 6자리로 인증서 사용을 잠금 해제해 로그인합니다.
  PBKDF2 저장·5회 잠금·기기 바인딩·보안 키패드(배열 셔플)로 보호합니다. `security/PinManager.kt`
- **E2E 필드 암호화**: 비밀번호·PIN 등 민감 필드를 TLS 위에 한 겹 더(AES-256-GCM + RSA-OAEP) 암호화해
  서버 앱단에서만 복호화합니다. `security/E2ECrypto.kt`
- 서버가 없어 "서버 검증"은 `security/MockBackend.kt`가 기기 안에서 흉내 냅니다. **암호 연산 자체는 실제**
  (ECDSA 서명·검증, RSA/AES 복호화)이고 네트워크만 로컬입니다.

## 보안 실습 (취약점 진단)
각 취약 시나리오는 **마이페이지 → 보안 실습 설정**에서 **취약 ↔ 안전 모드**로 전환할 수 있어,
같은 기능을 두 모드로 비교하며 진단합니다(기본값: 전부 취약 ON). 총 20개 시나리오.
구현한 취약점과 금융보안원 점검번호 매핑, 재현 방법은 [`docs/security/SECURITY-LAB.md`](docs/security/SECURITY-LAB.md) 참고.

> ⚠️ 교육용입니다. 실제 서버·실제 금융데이터가 없고 모든 처리는 기기 내부(로컬)에서만 일어나요.
> 운영 배포 시 `security/VulnConfig.kt`와 각 vulnerable 분기를 제거해야 합니다.

## APK 자동 빌드
GitHub에 push하면 Actions가 debug APK를 빌드해요.
저장소 **Actions 탭 → 최근 실행 → Artifacts → labbit-debug-apk** 에서 내려받아 설치할 수 있어요.

## 문서 (docs/)
| 문서 | 내용 |
| --- | --- |
| [`docs/api/openapi.yaml`](docs/api/openapi.yaml) | 전체 API 명세 (OpenAPI 3.0, v1.2) |
| [`docs/api/README.md`](docs/api/README.md) | 화면별 API 매핑·공통 규칙·이체 흐름 |
| [`docs/security/SECURITY-LAB.md`](docs/security/SECURITY-LAB.md) | 보안 실습 시나리오 ↔ 점검번호 매핑, 재현 방법 |
| [`docs/review/개발사항정의서_리뷰.md`](docs/review/개발사항정의서_리뷰.md) | 웹팀 개발사항정의서 ↔ 앱 정합성 리뷰 |
| [`docs/review/모바일_상세명세_작성가이드.md`](docs/review/모바일_상세명세_작성가이드.md) | 'API기준 상세' 시트 모바일팀 작성·수정·제안 가이드 |
