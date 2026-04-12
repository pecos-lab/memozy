-- ============================================================
-- Memozy 1:1 Sync Schema (Room DB Mirror)
-- 단방향 백업: 기기 → Supabase (upsert 기반)
-- ============================================================

-- 0. 기존 JSON 덤프 방식 테이블 제거
DROP TABLE IF EXISTS public.backup_data CASCADE;
DROP TABLE IF EXISTS public.backups CASCADE;
DROP FUNCTION IF EXISTS public.enforce_backup_limit() CASCADE;

-- ============================================================
-- 1. category
-- ============================================================
CREATE TABLE public.category (
    id          INT NOT NULL,
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name        TEXT NOT NULL,
    PRIMARY KEY (user_id, id)
);

ALTER TABLE public.category ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own categories"
    ON public.category FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 2. memo
-- ============================================================
CREATE TABLE public.memo (
    id              INT NOT NULL,
    user_id         UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name            TEXT NOT NULL,
    category_id     INT NOT NULL,
    content         TEXT NOT NULL,
    created_at      BIGINT NOT NULL DEFAULT 0,
    updated_at      BIGINT NOT NULL DEFAULT 0,
    format          TEXT NOT NULL DEFAULT 'PLAIN',
    is_pinned       BOOLEAN NOT NULL DEFAULT false,
    audio_path      TEXT,
    styles          TEXT,
    youtube_url     TEXT,
    deleted_at      BIGINT,
    reminder_at     BIGINT,
    summary_content TEXT,
    PRIMARY KEY (user_id, id)
);

CREATE INDEX idx_memo_user_updated ON public.memo(user_id, updated_at DESC);

ALTER TABLE public.memo ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own memos"
    ON public.memo FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 3. chat_session
-- ============================================================
CREATE TABLE public.chat_session (
    id          INT NOT NULL,
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    title       TEXT NOT NULL,
    created_at  BIGINT NOT NULL DEFAULT 0,
    updated_at  BIGINT NOT NULL DEFAULT 0,
    category    TEXT NOT NULL DEFAULT 'general',
    PRIMARY KEY (user_id, id)
);

ALTER TABLE public.chat_session ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own chat sessions"
    ON public.chat_session FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 4. chat_message
-- ============================================================
CREATE TABLE public.chat_message (
    id          INT NOT NULL,
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    session_id  INT NOT NULL,
    role        TEXT NOT NULL,
    content     TEXT NOT NULL,
    timestamp   BIGINT NOT NULL DEFAULT 0,
    metadata    TEXT,
    PRIMARY KEY (user_id, id)
);

CREATE INDEX idx_chat_message_session ON public.chat_message(user_id, session_id);

ALTER TABLE public.chat_message ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own chat messages"
    ON public.chat_message FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 5. youtube_summary (composite PK: videoId + mode + language)
-- ============================================================
CREATE TABLE public.youtube_summary (
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    video_id    TEXT NOT NULL,
    mode        TEXT NOT NULL DEFAULT 'SIMPLE',
    language    TEXT NOT NULL DEFAULT 'ko',
    url         TEXT NOT NULL,
    summary     TEXT NOT NULL,
    created_at  BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, video_id, mode, language)
);

ALTER TABLE public.youtube_summary ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own youtube summaries"
    ON public.youtube_summary FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 6. ai_usage
-- ============================================================
CREATE TABLE public.ai_usage (
    id          BIGINT NOT NULL,
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    feature     TEXT NOT NULL,
    used_at     BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, id)
);

ALTER TABLE public.ai_usage ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own ai usage"
    ON public.ai_usage FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- 7. backup_metadata (백업 시점 관리)
-- ============================================================
CREATE TABLE public.backup_metadata (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    device_name     TEXT NOT NULL,
    app_version     TEXT NOT NULL,
    db_version      INT NOT NULL,
    memo_count      INT NOT NULL DEFAULT 0,
    total_rows      INT NOT NULL DEFAULT 0,
    size_bytes      BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX idx_backup_metadata_user ON public.backup_metadata(user_id, created_at DESC);

ALTER TABLE public.backup_metadata ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own backup metadata"
    ON public.backup_metadata FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- 사용자당 최대 10개 백업 메타 유지
CREATE OR REPLACE FUNCTION public.enforce_backup_metadata_limit()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM public.backup_metadata
    WHERE id IN (
        SELECT id FROM public.backup_metadata
        WHERE user_id = NEW.user_id
        ORDER BY created_at DESC
        OFFSET 10
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_enforce_backup_metadata_limit
    AFTER INSERT ON public.backup_metadata
    FOR EACH ROW
    EXECUTE FUNCTION public.enforce_backup_metadata_limit();
