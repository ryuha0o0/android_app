package com.moa.finance.security

/**
 * 금융인증서 로그인 연결.
 *
 * 설계: 인증서의 개인키는 Android Keystore(하드웨어) 안에서만 쓰이고 추출되지 않습니다(KeystoreManager).
 * 가입/최초 로그인 때 공개키를 서버(MockBackend)에 등록하고, 로그인 때 1회성 챌린지를 개인키로 서명해
 * 서버가 등록 공개키로 검증합니다(챌린지-응답). 생체·간편비번은 이 개인키 사용을 잠금 해제하는 수단입니다.
 */
object CertificateManager {

    /**
     * 가입/최초 로그인 시: 기기 인증서(키쌍) 보장 + 공개키·연계정보(CI) 등록.
     * 실제 CA 대신 테스트 계정의 CI를 인증서에 바인딩합니다.
     */
    fun issueAndRegister(userId: String, ci: String) {
        KeystoreManager.ensureKey(requireBiometric = false)
        val pub = KeystoreManager.publicKeyBase64() ?: error("공개키 생성 실패")
        // E2E: 인증서 발급 정보(CI)를 평문이 아닌 암호화 봉투로 전송 → 서버 앱단에서만 복호화
        val env = E2ECrypto.encryptField(ci, MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID)
        val ciForServer = MockBackend.e2eDecrypt(env)
        MockBackend.registerDeviceKey(userId, ciForServer, pub)
    }

    fun isRegistered(userId: String): Boolean = KeystoreManager.hasKey() && MockBackend.hasRegisteredKey(userId)

    /**
     * 인증서 로그인: 챌린지 수신 → 개인키 서명 → 서버 검증.
     * @return 성공 여부
     */
    fun login(userId: String): Boolean {
        if (!KeystoreManager.hasKey()) return false
        val (challengeId, challenge) = MockBackend.issueChallenge()
        val signature = KeystoreManager.sign(challenge)
        return MockBackend.verifyLogin(userId, challengeId, signature)
    }
}
