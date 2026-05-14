-- ============================================================
-- App Store Guideline 5.1.1(v) — 사용자 계정 삭제 RPC
-- ============================================================
-- 클라이언트 SDK 는 service_role 권한이 없으므로 auth.users 행을
-- 직접 지울 수 없다. SECURITY DEFINER 함수로 auth.uid() 본인 행만
-- 삭제하도록 격상한다. 모든 public.* 테이블이 user_id REFERENCES
-- auth.users(id) ON DELETE CASCADE 로 묶여 있어 행 하나 삭제로
-- 메모 / 카테고리 / 채팅 / 요약 / ai_usage / backup_metadata /
-- user_subscriptions 가 함께 정리된다.

CREATE OR REPLACE FUNCTION public.delete_user_account()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth
AS $$
DECLARE
    uid uuid := auth.uid();
BEGIN
    IF uid IS NULL THEN
        RAISE EXCEPTION 'unauthorized' USING ERRCODE = '42501';
    END IF;
    DELETE FROM auth.users WHERE id = uid;
END;
$$;

-- 인증된 사용자에게만 실행 권한 부여 (anon / public 차단).
REVOKE ALL ON FUNCTION public.delete_user_account() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.delete_user_account() TO authenticated;
