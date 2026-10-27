# 모아 (Moa) — 금융 앱

Figma 디자인을 바탕으로 만든 Kotlin + Jetpack Compose 안드로이드 금융 앱입니다.

## 실행 방법
1. Android Studio(Ladybug 이상)에서 이 폴더를 **Open** 합니다.
2. Gradle Sync가 끝나면 에뮬레이터나 기기를 선택하고 ▶ Run 을 누릅니다.

- compileSdk / targetSdk 35, minSdk 26, JDK 17
- AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Navigation Compose 2.8.5

## 화면 구성
| 화면 | 파일 |
| --- | --- |
| 로그인 | `ui/screens/LoginScreen.kt` |
| 회원가입(본인 확인) | `ui/screens/SignUpScreen.kt` |
| 홈 | `ui/screens/HomeScreen.kt` |
| 조회 (전체계좌 / 거래내역 / 이체결과 / 해지계좌) | `ui/screens/InquiryScreen.kt` |
| 이체 / 출금 | `ui/screens/TransferScreen.kt` |
| 카드 (카드 신청 / 내 카드 관리) | `ui/screens/CardScreen.kt` |
| 마이페이지 | `ui/screens/MyPageScreen.kt` |
| 회원정보 수정 (기본정보 / 비밀번호 변경) | `ui/screens/EditProfileScreen.kt` |
| 계좌 관리 | `ui/screens/AccountManageScreen.kt` |

내비게이션과 하단 탭은 `ui/navigation/MoaApp.kt`, 공통 컴포넌트는 `ui/components/`, 색상은 `ui/theme/Color.kt`에 있습니다.

## 데이터
서버 없이 `data/MoaRepository.kt`의 메모리 데이터(디자인의 샘플 값)로 동작합니다.
이체·출금을 하면 잔액, 거래내역, 이체결과가 실제로 갱신되고, 대표계좌 설정·계좌 별명 변경·카드 일시정지 등도 앱 안에서 반영됩니다.
실제 서비스로 연결하려면 이 저장소 객체를 API 호출로 교체하면 됩니다.

## 보안 실습 (취약점 진단)
이 앱은 금융보안원 전자금융기반시설 평가기준 등 점검항목을 직접 진단하는 **교육용 앱**입니다.
각 취약 시나리오는 **마이페이지 → 보안 실습 설정**에서 취약/안전 모드로 전환할 수 있어요.
구현한 취약점과 점검번호 매핑은 [`docs/security/SECURITY-LAB.md`](docs/security/SECURITY-LAB.md)를 참고하세요.

> ⚠️ 교육용입니다. 실제 서버·실제 금융데이터가 없고 모든 처리는 기기 내부(로컬)에서만 일어나요.
> 운영 배포 시 `security/VulnConfig.kt`와 vulnerable 분기를 제거해야 합니다.

## APK 자동 빌드
GitHub에 push하면 Actions가 debug APK를 빌드해요.
저장소 **Actions 탭 → 최근 실행 → Artifacts → labbit-debug-apk** 에서 내려받아 설치할 수 있어요.

## API 명세
서버 API 명세(OpenAPI 3.0)는 [`docs/api/openapi.yaml`](docs/api/openapi.yaml), 화면별 API 정리는 [`docs/api/README.md`](docs/api/README.md)에 있습니다.
