-- ============================================================
-- Memozy Cloud Backup Schema
-- Issue: #125
-- ============================================================

-- 1. backups: 백업 메타데이터 (목록 조회용)
CREATE TABLE public.backups (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    device_name TEXT NOT NULL,
    app_version TEXT NOT NULL,
    db_version  INT NOT NULL,
    memo_count  INT NOT NULL DEFAULT 0,
    size_bytes  BIGINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE INDEX idx_backups_user_id ON public.backups(user_id);
CREATE INDEX idx_backups_created_at ON public.backups(created_at DESC);

-- 2. backup_data: 백업 실제 데이터 (테이블별 JSON)
CREATE TABLE public.backup_data (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    backup_id   UUID REFERENCES public.backups(id) ON DELETE CASCADE NOT NULL,
    table_name  TEXT NOT NULL,
    data        JSONB NOT NULL,
    row_count   INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_backup_data_backup_id ON public.backup_data(backup_id);

-- 3. RLS: 사용자 본인 데이터만 접근 가능
ALTER TABLE public.backups ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own backups"
    ON public.backups FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

ALTER TABLE public.backup_data ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own backup data"
    ON public.backup_data FOR ALL
    USING (
        backup_id IN (SELECT id FROM public.backups WHERE user_id = auth.uid())
    );

-- 4. 백업 개수 제한 함수: 사용자당 최대 5개, 초과 시 가장 오래된 것 삭제
CREATE OR REPLACE FUNCTION public.enforce_backup_limit()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM public.backups
    WHERE id IN (
        SELECT id FROM public.backups
        WHERE user_id = NEW.user_id
        ORDER BY created_at DESC
        OFFSET 5
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_enforce_backup_limit
    AFTER INSERT ON public.backups
    FOR EACH ROW
    EXECUTE FUNCTION public.enforce_backup_limit();
