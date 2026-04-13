import "@supabase/functions-js/edge-runtime.d.ts";
import { verifyAppAuth } from "../_shared/auth.ts";

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY")!;
const GEMINI_MODEL = Deno.env.get("GEMINI_MODEL") ?? "gemini-2.5-flash";
const FALLBACK_MODELS = ["gemini-2.5-flash-lite", "gemini-2.0-flash"];
const GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

async function callGeminiStream(model: string, body: unknown): Promise<Response> {
  const url = `${GEMINI_BASE_URL}/models/${model}:streamGenerateContent?alt=sse&key=${GEMINI_API_KEY}`;
  return await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authError = verifyAppAuth(req);
  if (authError) return authError;

  try {
    const body = await req.json();

    // 1차: 기본 모델
    let geminiRes = await callGeminiStream(GEMINI_MODEL, body);

    // 503 또는 429면 fallback 모델 순차 시도
    if (geminiRes.status === 503 || geminiRes.status === 429 || geminiRes.status === 404) {
      for (const fallback of FALLBACK_MODELS) {
        geminiRes = await callGeminiStream(fallback, body);
        if (geminiRes.status !== 503 && geminiRes.status !== 429 && geminiRes.status !== 404) {
          break;
        }
      }
    }

    if (!geminiRes.ok) {
      const errorBody = await geminiRes.text();
      return new Response(errorBody, {
        status: geminiRes.status,
        headers: { "Content-Type": "application/json" },
      });
    }

    return new Response(geminiRes.body, {
      status: 200,
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache",
        "Connection": "keep-alive",
      },
    });
  } catch (e) {
    return new Response(
      JSON.stringify({ error: (e as Error).message }),
      { status: 500, headers: { "Content-Type": "application/json" } },
    );
  }
});
