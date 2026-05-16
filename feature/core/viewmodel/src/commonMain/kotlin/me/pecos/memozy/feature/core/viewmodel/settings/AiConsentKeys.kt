package me.pecos.memozy.feature.core.viewmodel.settings

/**
 * AI 데이터 전송 동의 (App Store Guideline 5.1.1(i)/5.1.2(i)) 상태를
 * [PreferencesProvider] 에 저장할 때 쓰는 키. 모달 UI / Koin AI 게이트
 * 양쪽이 같은 키를 참조하도록 단일 정의.
 */
object AiConsentKeys {
    /** 사용자가 동의했는지 여부 (true 면 AI 호출 허용). */
    const val GIVEN = "ai_consent_given"

    /** 사용자가 모달에서 한 번이라도 선택을 마쳤는지 (true 면 재진입 시 모달 안 띄움). */
    const val DECIDED = "ai_consent_decided"
}
