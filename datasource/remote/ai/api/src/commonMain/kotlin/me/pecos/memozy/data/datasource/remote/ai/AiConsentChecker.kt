package me.pecos.memozy.data.datasource.remote.ai

/**
 * App Store Guideline 5.1.1(i) / 5.1.2(i): 사용자 데이터를 제3자 AI 서비스로
 * 전송하기 전에 사전 동의를 받아야 함. 모든 AI 호출 진입점에서
 * [isConsentGiven] 을 확인해 미동의 시 [AIException.ConsentRequiredException] 을 던진다.
 */
interface AiConsentChecker {
    fun isConsentGiven(): Boolean
}
