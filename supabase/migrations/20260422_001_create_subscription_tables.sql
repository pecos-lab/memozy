-- ============================================================
-- Subscription & Payment System Tables
-- Issue #294: Real payment system implementation
-- ============================================================

-- ============================================================
-- 1. user_subscriptions
-- Stores current subscription state per user
-- ============================================================
CREATE TABLE public.user_subscriptions (
    user_id             UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    tier                TEXT NOT NULL DEFAULT 'FREE' CHECK (tier IN ('FREE', 'PRO')),
    product_id          TEXT,  -- e.g., 'pro_monthly', 'pro_yearly'
    platform            TEXT CHECK (platform IN ('android', 'ios')),  -- which platform purchased from
    purchase_token      TEXT,  -- Google Play: purchase token, iOS: transaction ID
    expires_at          TIMESTAMPTZ,  -- subscription expiration time
    auto_renewing       BOOLEAN DEFAULT true,
    grace_period_ends_at TIMESTAMPTZ,  -- grace period for payment failures
    created_at          TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at          TIMESTAMPTZ DEFAULT now() NOT NULL,

    -- Ensure only PRO users have platform/token data
    CONSTRAINT valid_pro_subscription CHECK (
        (tier = 'FREE' AND product_id IS NULL AND purchase_token IS NULL AND expires_at IS NULL) OR
        (tier = 'PRO' AND product_id IS NOT NULL AND platform IS NOT NULL AND purchase_token IS NOT NULL)
    )
);

CREATE INDEX idx_user_subscriptions_expires ON public.user_subscriptions(expires_at)
    WHERE tier = 'PRO' AND expires_at IS NOT NULL;

ALTER TABLE public.user_subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users read own subscription"
    ON public.user_subscriptions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users cannot modify subscriptions directly"
    ON public.user_subscriptions FOR INSERT
    WITH CHECK (false);  -- Only server (via service role) can modify

CREATE POLICY "Users cannot update subscriptions directly"
    ON public.user_subscriptions FOR UPDATE
    USING (false);  -- Only server (via service role) can modify

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_subscriptions_updated_at
    BEFORE UPDATE ON public.user_subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- ============================================================
-- 2. subscription_transactions
-- Audit log for all subscription events (purchases, renewals, cancellations, refunds)
-- ============================================================
CREATE TABLE public.subscription_transactions (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    event_type          TEXT NOT NULL CHECK (event_type IN (
        'purchase',      -- new subscription purchase
        'renewal',       -- automatic renewal
        'cancellation',  -- user cancelled (still active until expires_at)
        'expiration',    -- subscription expired
        'refund',        -- purchase refunded
        'restore',       -- purchase restored from receipt
        'grace_period'   -- entered grace period due to payment failure
    )),
    platform            TEXT NOT NULL CHECK (platform IN ('android', 'ios')),
    product_id          TEXT NOT NULL,
    purchase_token      TEXT NOT NULL,

    -- Store original receipt data for auditing
    receipt_data        JSONB,  -- Full receipt validation response

    -- Transaction metadata
    transaction_date    TIMESTAMPTZ NOT NULL,
    expires_at          TIMESTAMPTZ,
    is_trial            BOOLEAN DEFAULT false,

    created_at          TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX idx_subscription_transactions_user ON public.subscription_transactions(user_id, created_at DESC);
CREATE INDEX idx_subscription_transactions_token ON public.subscription_transactions(purchase_token);

ALTER TABLE public.subscription_transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users read own transactions"
    ON public.subscription_transactions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users cannot modify transactions"
    ON public.subscription_transactions FOR INSERT
    WITH CHECK (false);  -- Only server (via service role) can write

-- ============================================================
-- 3. Initialize existing users with FREE tier
-- ============================================================
INSERT INTO public.user_subscriptions (user_id, tier)
SELECT id, 'FREE'
FROM auth.users
WHERE id NOT IN (SELECT user_id FROM public.user_subscriptions)
ON CONFLICT (user_id) DO NOTHING;

-- ============================================================
-- 4. Auto-create FREE subscription for new users
-- ============================================================
CREATE OR REPLACE FUNCTION public.create_user_subscription()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.user_subscriptions (user_id, tier)
    VALUES (NEW.id, 'FREE')
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_create_user_subscription
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.create_user_subscription();

-- ============================================================
-- 5. Helper function: Check if subscription is active
-- ============================================================
CREATE OR REPLACE FUNCTION public.is_subscription_active(p_user_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_tier TEXT;
    v_expires_at TIMESTAMPTZ;
    v_grace_period_ends_at TIMESTAMPTZ;
BEGIN
    SELECT tier, expires_at, grace_period_ends_at
    INTO v_tier, v_expires_at, v_grace_period_ends_at
    FROM public.user_subscriptions
    WHERE user_id = p_user_id;

    IF v_tier = 'FREE' THEN
        RETURN false;
    END IF;

    -- Check if in grace period
    IF v_grace_period_ends_at IS NOT NULL AND now() <= v_grace_period_ends_at THEN
        RETURN true;
    END IF;

    -- Check if not expired
    IF v_expires_at IS NULL OR now() <= v_expires_at THEN
        RETURN true;
    END IF;

    RETURN false;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
