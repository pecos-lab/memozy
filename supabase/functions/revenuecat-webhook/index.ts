import "@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.3";

// RevenueCat 대시보드에서 webhook URL 등록 시 Authorization 헤더 값 설정 →
// 이 환경변수와 정확히 일치해야 통과. RC 는 HMAC signing 미제공이라 단순 토큰 비교.
const RC_AUTH = Deno.env.get("REVENUECAT_WEBHOOK_AUTH");
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

// RevenueCat 대시보드에 정의된 entitlement id (Epic 섹션 1).
const ENTITLEMENT_PRO = "pro";

// ── 타입 ──────────────────────────────────────────────────────────────
interface RevenueCatEvent {
  type: string;
  id: string;
  app_user_id: string;
  product_id?: string;
  entitlement_ids?: string[] | null;
  expiration_at_ms?: number | null;
  purchased_at_ms?: number | null;
  event_timestamp_ms?: number | null;
  store?: string | null;
}

interface RevenueCatPayload {
  api_version?: string;
  event?: RevenueCatEvent;
}

type TierChange = "GRANT" | "REVOKE" | "NEUTRAL";

// ── 유틸 ──────────────────────────────────────────────────────────────
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function isSupabaseUserId(value: string): boolean {
  return UUID_REGEX.test(value);
}

// 이벤트 타입 → tier 방향 매핑.
// Epic 섹션 5 라이프사이클: 갱신/만료/은혜기간/취소 모두 여기서 분기.
function deriveTierChange(eventType: string): TierChange {
  switch (eventType) {
    case "INITIAL_PURCHASE":
    case "RENEWAL":
    case "UNCANCELLATION":
    case "PRODUCT_CHANGE":
    case "SUBSCRIPTION_EXTENDED":
    case "TEMPORARY_ENTITLEMENT_GRANT":
    case "TRANSFER":
      return "GRANT";
    case "EXPIRATION":
    case "CANCELLATION":
      return "REVOKE";
    case "BILLING_ISSUE":
    case "SUBSCRIPTION_PAUSED":
      // 은혜기간/일시정지 — 진행 중에는 entitlement 유지. RC 가 종료 시 EXPIRATION 으로 finalize.
      return "NEUTRAL";
    default:
      return "NEUTRAL";
  }
}

function msToIso(ms: number | null | undefined): string | null {
  if (ms == null) return null;
  return new Date(ms).toISOString();
}

function jsonResponse(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

// ── 핸들러 ────────────────────────────────────────────────────────────
Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return jsonResponse({ error: "Method Not Allowed" }, 405);
  }

  const got = req.headers.get("authorization");
  if (!RC_AUTH || !got || got !== RC_AUTH) {
    return jsonResponse({ error: "Unauthorized" }, 401);
  }

  let payload: RevenueCatPayload;
  try {
    payload = await req.json();
  } catch {
    return jsonResponse({ error: "Invalid JSON" }, 400);
  }

  const event = payload.event;
  if (!event?.type || !event?.id || !event?.app_user_id) {
    return jsonResponse({ error: "Missing event fields" }, 400);
  }

  const userId = event.app_user_id;

  // 익명 RC user (logIn 전 발급된 $RCAnonymousID:...) 또는 외부 식별자는 무시.
  // 클라이언트가 Supabase 로그인 시점에 awaitLogIn(supabaseUserId) 호출하므로
  // PRO 결제 후 webhook 의 app_user_id 는 항상 Supabase UUID 형식이어야 함.
  if (!isSupabaseUserId(userId)) {
    return jsonResponse({ ignored: "non_supabase_user", app_user_id: userId }, 200);
  }

  // entitlement_ids 에 "pro" 가 없으면 우리가 관심 없는 이벤트(도네이션 등).
  // entitlement_ids 가 null/undefined 인 경우(NON_RENEWING_PURCHASE 등)도 동일 처리.
  if (!event.entitlement_ids?.includes(ENTITLEMENT_PRO)) {
    return jsonResponse({ ignored: "other_entitlement", type: event.type }, 200);
  }

  const supabase = createClient(SUPABASE_URL, SERVICE_ROLE_KEY);

  // Idempotency — RC 가 동일 event.id 를 재전송할 수 있음.
  const { data: existing, error: selectError } = await supabase
    .from("user_subscriptions")
    .select("last_event_id")
    .eq("user_id", userId)
    .maybeSingle();

  if (selectError) {
    console.error("Select user_subscriptions failed:", selectError);
    return jsonResponse({ error: selectError.message }, 500);
  }

  if (existing?.last_event_id === event.id) {
    return jsonResponse({ idempotent: true, event_id: event.id }, 200);
  }

  const tierChange = deriveTierChange(event.type);

  // upsert payload 구성 — NEUTRAL 이면 last_event_* 만 갱신.
  const update: Record<string, unknown> = {
    user_id: userId,
    last_event_id: event.id,
    last_event_type: event.type,
    last_event_at: msToIso(event.event_timestamp_ms) ?? new Date().toISOString(),
  };

  if (tierChange === "GRANT") {
    update.tier = "PRO";
    update.entitlement_id = ENTITLEMENT_PRO;
    update.product_id = event.product_id ?? null;
    update.store = event.store ?? null;
    update.purchased_at = msToIso(event.purchased_at_ms);
    update.expires_at = msToIso(event.expiration_at_ms);
  } else if (tierChange === "REVOKE") {
    update.tier = "FREE";
    update.entitlement_id = null;
    update.product_id = null;
    update.store = null;
    update.purchased_at = null;
    update.expires_at = null;
  }

  const { error: upsertError } = await supabase
    .from("user_subscriptions")
    .upsert(update, { onConflict: "user_id" });

  if (upsertError) {
    console.error("Upsert user_subscriptions failed:", upsertError);
    return jsonResponse({ error: upsertError.message }, 500);
  }

  return jsonResponse({
    ok: true,
    type: event.type,
    tier_change: tierChange,
  }, 200);
});
