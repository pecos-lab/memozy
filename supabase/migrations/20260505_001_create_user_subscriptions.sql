-- ============================================================
-- RevenueCat 구독 캐시 테이블
-- Issue: #294 (Epic 섹션 4)
--
-- revenuecat-webhook Edge Function 이 entitlement 변경 이벤트를 받아 이 테이블에 캐시.
-- 클라이언트의 진실은 RC SDK(`awaitCustomerInfoResult()`) — 이 테이블은 서버측
-- gating/조회용 캐시일 뿐. 정합성 깨질 시 클라이언트가 RC 로 재동기화.
-- ============================================================

CREATE TABLE public.user_subscriptions (
    user_id              UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    tier                 TEXT NOT NULL DEFAULT 'FREE' CHECK (tier IN ('FREE', 'PRO')),
    entitlement_id       TEXT,
    product_id           TEXT,
    store                TEXT,           -- APP_STORE / PLAY_STORE / STRIPE / PROMOTIONAL / AMAZON / MAC_APP_STORE
    purchased_at         TIMESTAMPTZ,
    expires_at           TIMESTAMPTZ,
    last_event_id        TEXT,           -- RC event.id — webhook idempotency key
    last_event_type      TEXT,
    last_event_at        TIMESTAMPTZ,
    updated_at           TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX idx_user_subscriptions_expires ON public.user_subscriptions(expires_at)
    WHERE tier = 'PRO' AND expires_at IS NOT NULL;

ALTER TABLE public.user_subscriptions ENABLE ROW LEVEL SECURITY;

-- 본인 행만 읽기 — Edge Function gating / 클라이언트 조회 모두 cover
CREATE POLICY "Users read own subscription"
    ON public.user_subscriptions FOR SELECT
    USING (auth.uid() = user_id);

-- 클라이언트 직접 쓰기 차단 — service_role(webhook) 만 INSERT/UPDATE/DELETE 가능
CREATE POLICY "Block client writes"
    ON public.user_subscriptions FOR ALL
    USING (false)
    WITH CHECK (false);

-- updated_at 자동 갱신
CREATE OR REPLACE FUNCTION public.update_user_subscriptions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_subscriptions_updated_at
    BEFORE UPDATE ON public.user_subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION public.update_user_subscriptions_updated_at();
